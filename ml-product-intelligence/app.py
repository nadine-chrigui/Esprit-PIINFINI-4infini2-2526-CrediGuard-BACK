import json
from pathlib import Path

import joblib
import pandas as pd
from flask import Flask, jsonify, request
from flask_cors import CORS

app = Flask(__name__)
CORS(app)

BASE_DIR = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "models" / "product_intelligence_model.joblib"
METADATA_PATH = BASE_DIR / "models" / "product_intelligence_metadata.json"
MODEL_BUNDLE = None
MODEL_METADATA = None


VALID_RISK_LEVELS = {"LOW", "MEDIUM", "HIGH"}
VALID_PERFORMANCE_LABELS = {"BEST_SELLER", "STABLE", "SLOW_MOVING", "AT_RISK"}
VALID_ACTIONS = {"RESTOCK", "PROMOTE", "MONITOR", "KEEP"}


def load_model():
    global MODEL_BUNDLE, MODEL_METADATA

    if not MODEL_PATH.exists():
        MODEL_BUNDLE = None
        MODEL_METADATA = None
        return

    MODEL_BUNDLE = joblib.load(MODEL_PATH)

    if METADATA_PATH.exists():
        MODEL_METADATA = json.loads(METADATA_PATH.read_text(encoding="utf-8"))
    else:
        MODEL_METADATA = {"model_type": "Unknown", "rows": None}


load_model()


def to_number(payload, field, default=0):
    value = payload.get(field, default)
    if value is None:
        return default

    try:
        return float(value)
    except (TypeError, ValueError):
        raise ValueError(f"{field} must be a number")


def clamp(value, minimum, maximum):
    return max(minimum, min(maximum, value))


def format_stockout(days_to_stockout):
    if days_to_stockout >= 999:
        return "No stockout expected from current sales velocity"
    return f"estimated stockout in {days_to_stockout} day(s)"


def format_feature_name(feature):
    labels = {
        "stock": "current stock",
        "sales_last_7_days": "sales in last 7 days",
        "sales_last_30_days": "sales in last 30 days",
        "price": "product price",
    }
    return labels.get(feature, feature.replace("_", " "))


def prediction_confidence(classifier, features, output_index, predicted_label):
    try:
        probabilities_by_output = classifier.predict_proba(features)
        probabilities = probabilities_by_output[output_index][0]
        estimator = classifier.named_steps["model"].estimators_[output_index]
        classes = list(estimator.classes_)

        if predicted_label not in classes:
            return None

        return round(float(probabilities[classes.index(predicted_label)]) * 100)
    except Exception:
        return None


def strongest_feature_drivers(classifier, output_index, feature_values):
    try:
        feature_columns = MODEL_BUNDLE.get("feature_columns", list(feature_values.keys()))
        estimator = classifier.named_steps["model"].estimators_[output_index]
        importances = estimator.feature_importances_
        ranked = sorted(
            zip(feature_columns, importances),
            key=lambda item: item[1],
            reverse=True,
        )[:3]

        return [
            f"{format_feature_name(feature)}={feature_values.get(feature)}"
            for feature, importance in ranked
            if importance > 0
        ]
    except Exception:
        return []


def build_ml_explanations(
    stock,
    sales_last_7_days,
    sales_last_30_days,
    daily_velocity,
    days_to_stockout,
    recommended_restock,
    risk_level,
    performance_label,
    suggested_action,
    model_type,
    risk_confidence=None,
    action_confidence=None,
    drivers=None,
):
    stockout_text = format_stockout(days_to_stockout)
    confidence_text = f"{risk_confidence}% confidence" if risk_confidence is not None else "model confidence unavailable"
    action_confidence_text = f"{action_confidence}% confidence" if action_confidence is not None else "confidence unavailable"
    drivers_text = ", ".join(drivers or [])

    reasons = [
        (
            f"ML decision: {risk_level.lower()} stock risk ({confidence_text}) and "
            f"{suggested_action.lower()} action ({action_confidence_text})."
        )
    ]

    if drivers_text:
        reasons.append(f"Main ML drivers: {drivers_text}.")
    else:
        reasons.append(
            f"Main ML inputs: stock={stock}, sales 7d={sales_last_7_days}, sales 30d={sales_last_30_days}, velocity={daily_velocity:.2f}/day."
        )

    if suggested_action == "RESTOCK":
        reasons.append(
            f"Business reason: stock is {stock} with {stockout_text}; restock {recommended_restock} unit(s) to protect availability."
        )
    elif suggested_action == "PROMOTE":
        reasons.append(
            f"Business reason: score is {performance_label.replace('_', ' ').lower()} with limited recent demand; use a discount, bundle, or homepage placement."
        )
    elif suggested_action == "MONITOR":
        reasons.append(
            f"Business reason: {stockout_text}; monitor the next orders before changing price or stock."
        )
    else:
        reasons.append(
            "Business reason: stock and sales pattern look stable; keep current pricing and availability."
        )

    reasons.append(
        f"Observed data: stock {stock}, {sales_last_7_days} sale(s) in 7 days, {sales_last_30_days} sale(s) in 30 days."
    )
    reasons.append(f"Model used: {model_type}.")
    return reasons


def build_structured_ml_explanation(
    stock,
    sales_last_7_days,
    sales_last_30_days,
    daily_velocity,
    days_to_stockout,
    recommended_restock,
    risk_level,
    performance_label,
    suggested_action,
    model_type,
    risk_confidence=None,
    action_confidence=None,
    drivers=None,
):
    risk_text = f"{risk_confidence}% confidence" if risk_confidence is not None else "confidence unavailable"
    action_text = f"{action_confidence}% confidence" if action_confidence is not None else "confidence unavailable"

    ml_decision = (
        f"The trained model classified this product as {risk_level.lower()} stock risk "
        f"and selected {suggested_action.lower()} as the next admin action "
        f"({risk_text} for risk, {action_text} for action)."
    )

    if suggested_action == "RESTOCK":
        business_recommendation = (
            f"Restock {recommended_restock} unit(s) because current stock is {stock} and "
            f"the estimated stockout is {format_stockout(days_to_stockout).lower()}."
        )
    elif suggested_action == "PROMOTE":
        business_recommendation = (
            f"Promote this product because the model sees {performance_label.replace('_', ' ').lower()} performance. "
            "Use a small discount, bundle, or homepage placement to improve rotation."
        )
    elif suggested_action == "MONITOR":
        business_recommendation = (
            "Monitor this product after the next orders before changing stock or price."
        )
    else:
        business_recommendation = (
            "Keep the current strategy because stock and sales behavior look stable."
        )

    if not drivers:
        drivers = [
            f"current stock={stock}",
            f"sales in last 7 days={sales_last_7_days}",
            f"sales in last 30 days={sales_last_30_days}",
            f"sales velocity={daily_velocity:.2f}/day",
        ]

    return {
        "risk_confidence": risk_confidence,
        "action_confidence": action_confidence,
        "ml_decision": ml_decision,
        "main_drivers": drivers,
        "business_recommendation": business_recommendation,
        "model_type": model_type,
    }


def build_admin_explanations(
    stock,
    sales_last_7_days,
    sales_last_30_days,
    daily_velocity,
    days_to_stockout,
    recommended_restock,
    risk_level,
    performance_label,
    suggested_action,
    model_type=None,
):
    reasons = []
    stockout_text = format_stockout(days_to_stockout)

    if model_type:
        reasons.append(
            f"ML prediction: {risk_level.lower()} risk, {performance_label.replace('_', ' ').lower()} performance, "
            f"{suggested_action.lower()} action."
        )
    else:
        reasons.append(
            f"Rule analysis: {risk_level.lower()} risk, {performance_label.replace('_', ' ').lower()} performance, "
            f"{suggested_action.lower()} action."
        )

    reasons.append(
        f"Stock is {stock}, sales are {sales_last_7_days} in 7 days and {sales_last_30_days} in 30 days; {stockout_text}."
    )

    if stock <= 0:
        reasons.append("Product is out of stock, so restocking is urgent before accepting more demand.")
    elif risk_level == "HIGH":
        reasons.append("Risk is high because available stock is low compared with recent demand.")
    elif risk_level == "MEDIUM":
        reasons.append("Risk is medium: monitor the product because stock may become tight soon.")
    else:
        reasons.append("Stock coverage is comfortable for the current sales pace.")

    if suggested_action == "RESTOCK":
        reasons.append(f"Recommended admin action: restock {recommended_restock} unit(s) to cover roughly 30 days of demand.")
    elif suggested_action == "PROMOTE":
        if sales_last_30_days <= 3:
            reasons.append("Recommended admin action: promote this item because recent demand is weak.")
        else:
            reasons.append("Recommended admin action: promote this item with a small discount or bundle to accelerate rotation.")
    elif suggested_action == "MONITOR":
        reasons.append("Recommended admin action: check this product again after the next orders before changing price or stock.")
    else:
        reasons.append("Recommended admin action: keep current strategy; no urgent intervention is needed.")

    if daily_velocity > 0:
        reasons.append(f"Current sales velocity is about {daily_velocity:.2f} unit(s) per day.")
    if model_type:
        reasons.append(f"Model used: {model_type}.")

    return reasons


def predict_product_intelligence(payload):
    product_id = payload.get("product_id")
    stock = int(to_number(payload, "stock", 0))
    sales_last_7_days = int(to_number(payload, "sales_last_7_days", 0))
    sales_last_30_days = int(to_number(payload, "sales_last_30_days", 0))
    price = to_number(payload, "price", 0)

    daily_velocity = sales_last_7_days / 7 if sales_last_7_days > 0 else sales_last_30_days / 30
    days_to_stockout = 999 if daily_velocity <= 0 else max(1, int(stock // daily_velocity))

    if stock <= 0 or (daily_velocity > 0 and days_to_stockout <= 5):
        risk_level = "HIGH"
    elif days_to_stockout <= 14:
        risk_level = "MEDIUM"
    else:
        risk_level = "LOW"

    if risk_level == "HIGH" and sales_last_7_days > 0:
        performance_label = "AT_RISK"
    elif sales_last_30_days >= 50 or sales_last_7_days >= 15:
        performance_label = "BEST_SELLER"
    elif sales_last_30_days <= 3 and stock >= 20:
        performance_label = "SLOW_MOVING"
    else:
        performance_label = "STABLE"

    performance_score = 45
    performance_score += min(30, sales_last_30_days)
    performance_score += min(15, sales_last_7_days)

    if performance_label == "BEST_SELLER":
        performance_score += 15
    if performance_label == "SLOW_MOVING":
        performance_score -= 20
    if risk_level == "HIGH":
        performance_score -= 15
    if stock <= 0:
        performance_score -= 10
    if price > 0 and sales_last_30_days > 0:
        performance_score += 5

    performance_score = int(clamp(performance_score, 0, 100))

    if risk_level == "LOW" or daily_velocity <= 0:
        recommended_restock = 0
    else:
        target_stock_for_30_days = int((daily_velocity * 30) + 0.999)
        recommended_restock = max(0, target_stock_for_30_days - stock)

    if risk_level == "HIGH" or stock <= 0:
        suggested_action = "RESTOCK"
    elif performance_label == "SLOW_MOVING" or sales_last_30_days <= 3:
        suggested_action = "PROMOTE"
    elif risk_level == "MEDIUM":
        suggested_action = "MONITOR"
    else:
        suggested_action = "KEEP"

    reasons = build_admin_explanations(
        stock,
        sales_last_7_days,
        sales_last_30_days,
        daily_velocity,
        days_to_stockout,
        recommended_restock,
        risk_level,
        performance_label,
        suggested_action,
    )

    return {
        "product_id": product_id,
        "risk_level": risk_level,
        "days_to_stockout": days_to_stockout,
        "recommended_restock": recommended_restock,
        "performance_score": performance_score,
        "performance_label": performance_label,
        "suggested_action": suggested_action,
        "reasons": reasons,
        "risk_confidence": None,
        "action_confidence": None,
        "ml_decision": None,
        "main_drivers": [],
        "business_recommendation": reasons[2] if len(reasons) > 2 else None,
        "model_type": None,
    }


def predict_with_model(payload):
    if MODEL_BUNDLE is None:
        return None

    product_id = payload.get("product_id")
    stock = int(to_number(payload, "stock", 0))
    sales_last_7_days = int(to_number(payload, "sales_last_7_days", 0))
    sales_last_30_days = int(to_number(payload, "sales_last_30_days", 0))
    price = to_number(payload, "price", 0)

    features = pd.DataFrame(
        [
            {
                "stock": stock,
                "sales_last_7_days": sales_last_7_days,
                "sales_last_30_days": sales_last_30_days,
                "price": price,
            }
        ]
    )

    classifier = MODEL_BUNDLE["classifier"]
    regressor = MODEL_BUNDLE["regressor"]

    class_prediction = classifier.predict(features)[0]
    performance_score = int(clamp(round(regressor.predict(features)[0]), 0, 100))

    risk_level = str(class_prediction[0])
    performance_label = str(class_prediction[1])
    suggested_action = str(class_prediction[2])

    daily_velocity = sales_last_7_days / 7 if sales_last_7_days > 0 else sales_last_30_days / 30
    days_to_stockout = 999 if daily_velocity <= 0 else max(1, int(stock // daily_velocity))

    if risk_level == "LOW" or daily_velocity <= 0:
        recommended_restock = 0
    else:
        target_stock_for_30_days = int((daily_velocity * 30) + 0.999)
        recommended_restock = max(0, target_stock_for_30_days - stock)

    model_type = MODEL_METADATA.get("model_type", "trained model") if MODEL_METADATA else "trained model"
    feature_values = {
        "stock": stock,
        "sales_last_7_days": sales_last_7_days,
        "sales_last_30_days": sales_last_30_days,
        "price": price,
    }
    risk_confidence = prediction_confidence(classifier, features, 0, risk_level)
    action_confidence = prediction_confidence(classifier, features, 2, suggested_action)
    drivers = strongest_feature_drivers(classifier, 0, feature_values)

    reasons = build_ml_explanations(
        stock,
        sales_last_7_days,
        sales_last_30_days,
        daily_velocity,
        days_to_stockout,
        recommended_restock,
        risk_level,
        performance_label,
        suggested_action,
        model_type,
        risk_confidence,
        action_confidence,
        drivers,
    )
    structured_explanation = build_structured_ml_explanation(
        stock,
        sales_last_7_days,
        sales_last_30_days,
        daily_velocity,
        days_to_stockout,
        recommended_restock,
        risk_level,
        performance_label,
        suggested_action,
        model_type,
        risk_confidence,
        action_confidence,
        drivers,
    )

    return {
        "product_id": product_id,
        "risk_level": risk_level,
        "days_to_stockout": days_to_stockout,
        "recommended_restock": recommended_restock,
        "performance_score": performance_score,
        "performance_label": performance_label,
        "suggested_action": suggested_action,
        "reasons": reasons,
        **structured_explanation,
    }


@app.get("/health")
def health():
    return jsonify({
        "status": "UP",
        "service": "product-intelligence-ml",
        "model_loaded": MODEL_BUNDLE is not None,
    })


@app.get("/model-info")
def model_info():
    if MODEL_BUNDLE is None:
        return jsonify({
            "model_loaded": False,
            "mode": "RULE_BASED_FALLBACK",
            "message": "No trained model found. Run train_model.py first.",
        })

    return jsonify({
        "model_loaded": True,
        "mode": "TRAINED_MODEL",
        "metadata": MODEL_METADATA,
    })


@app.post("/predict/product-intelligence")
def predict():
    payload = request.get_json(silent=True)

    if not payload:
        return jsonify({"error": "JSON body is required"}), 400

    try:
        result = predict_with_model(payload) or predict_product_intelligence(payload)
    except ValueError as exc:
        return jsonify({"error": str(exc)}), 400

    if result["risk_level"] not in VALID_RISK_LEVELS:
        return jsonify({"error": "Invalid risk level generated"}), 500
    if result["performance_label"] not in VALID_PERFORMANCE_LABELS:
        return jsonify({"error": "Invalid performance label generated"}), 500
    if result["suggested_action"] not in VALID_ACTIONS:
        return jsonify({"error": "Invalid suggested action generated"}), 500

    return jsonify(result)


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001, debug=True)
