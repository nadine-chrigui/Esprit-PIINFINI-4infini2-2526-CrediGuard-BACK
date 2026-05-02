"""
Step 12: Production Flask API
Endpoints:
  POST /predict           – Single customer prediction
  POST /predict_batch     – Batch predictions (JSON list)
  GET  /model_info        – Model metadata and version
  GET  /feature_importance – Top 10 features
  GET  /model_performance  – Evaluation metrics
  GET  /health            – Health check
"""

import json
import logging
import os
import pickle
from pathlib import Path

import numpy as np
import pandas as pd
from flask import Flask, jsonify, request
from flask_cors import CORS

# ── Paths ─────────────────────────────────────────────────────
BASE_DIR = Path(__file__).resolve().parent.parent
MODELS_DIR = BASE_DIR / "models"
REPORTS_DIR = BASE_DIR / "reports"

# ── Logging ───────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s",
)
logger = logging.getLogger(__name__)

# ── Flask app ─────────────────────────────────────────────────
app = Flask(__name__)
CORS(app)

# ── State ─────────────────────────────────────────────────────
MODEL = None
PREPROCESSOR = None
LABEL_ENCODER = None
MODEL_INFO: dict = {}
FEATURE_IMPORTANCE: dict = {}
EVAL_METRICS: dict = {}

REQUIRED_FEATURES = [
    "age", "income", "premium_amount", "policy_tenure_months",
    "num_missed_payments_12m", "avg_payment_delay_days",
    "claims_frequency", "credit_score", "payment_consistency_score",
    "account_age_months", "policy_type", "location",
]


# ── Model loading ─────────────────────────────────────────────

def _derive_features(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    df["premium_to_income_ratio"] = (
        df["premium_amount"] / df["income"].replace(0, np.nan)
    ).round(4).fillna(0)
    df["payment_risk_score"] = (
        df["num_missed_payments_12m"] * 10
        + df["avg_payment_delay_days"] * 0.5
        + (1 - df["payment_consistency_score"]) * 20
    ).round(2)
    df["tenure_to_age_ratio"] = (
        df["policy_tenure_months"] / (df["age"] * 12)
    ).round(4)
    return df


def load_model_artifacts():
    global MODEL, PREPROCESSOR, LABEL_ENCODER, MODEL_INFO, FEATURE_IMPORTANCE, EVAL_METRICS

    # Preprocessor
    prep_path = MODELS_DIR / "preprocessor.pkl"
    if prep_path.exists():
        with open(prep_path, "rb") as f:
            data = pickle.load(f)
        PREPROCESSOR = data["preprocessor"]
        LABEL_ENCODER = data["label_encoder"]
        logger.info("Preprocessor loaded.")
    else:
        logger.warning("preprocessor.pkl not found.")

    # Best model info
    info_path = MODELS_DIR / "best_model_info.json"
    if info_path.exists():
        with open(info_path) as f:
            info = json.load(f)
        is_nn = info.get("is_nn", False)
        if is_nn:
            import tensorflow as tf
            nn_path = MODELS_DIR / "best_model.keras"
            if nn_path.exists():
                MODEL = tf.keras.models.load_model(str(nn_path))
                logger.info("Neural network best model loaded.")
        else:
            pkl_path = MODELS_DIR / "best_model.pkl"
            if pkl_path.exists():
                import joblib
                MODEL = joblib.load(pkl_path)
                logger.info("Sklearn best model loaded.")

    # Model card
    card_path = MODELS_DIR / "model_card.json"
    if card_path.exists():
        with open(card_path) as f:
            MODEL_INFO = json.load(f)

    # Evaluation metrics
    metrics_path = REPORTS_DIR / "evaluation_metrics.json"
    if metrics_path.exists():
        with open(metrics_path) as f:
            EVAL_METRICS = json.load(f)

    # Feature importance (from training results)
    try:
        best_name = MODEL_INFO.get("model_name", "random_forest")
        if best_name == "random_forest":
            best_pkl = MODELS_DIR / "random_forest.pkl"
        elif best_name == "xgboost":
            best_pkl = MODELS_DIR / "xgboost.pkl"
        else:
            best_pkl = None

        if best_pkl and best_pkl.exists() and PREPROCESSOR is not None:
            with open(best_pkl, "rb") as f:
                tree_model = pickle.load(f)
            feat_names = list(PREPROCESSOR.get_feature_names_out())
            importances = tree_model.feature_importances_
            idx = np.argsort(importances)[::-1][:10]
            FEATURE_IMPORTANCE = {
                feat_names[i]: round(float(importances[i]), 6) for i in idx
            }
    except Exception as e:
        logger.warning(f"Feature importance not loaded: {e}")


# ── Input validation ──────────────────────────────────────────

def validate_input(data: dict) -> tuple:
    """Return (cleaned_dict, error_message). error_message is None if valid."""
    missing = [f for f in REQUIRED_FEATURES if f not in data]
    if missing:
        return None, f"Missing required fields: {missing}"

    try:
        cleaned = {
            "age": int(data["age"]),
            "income": float(data["income"]),
            "premium_amount": float(data["premium_amount"]),
            "policy_tenure_months": int(data["policy_tenure_months"]),
            "num_missed_payments_12m": int(data["num_missed_payments_12m"]),
            "avg_payment_delay_days": float(data["avg_payment_delay_days"]),
            "claims_frequency": float(data["claims_frequency"]),
            "credit_score": float(data["credit_score"]),
            "payment_consistency_score": float(data["payment_consistency_score"]),
            "account_age_months": int(data["account_age_months"]),
            "policy_type": str(data["policy_type"]),
            "location": str(data["location"]),
        }
    except (ValueError, TypeError):
        return None, "One or more field values have an invalid type or format"

    return cleaned, None


def predict_single(data: dict) -> dict:
    df = pd.DataFrame([data])
    df = _derive_features(df)
    X = PREPROCESSOR.transform(df)
    if hasattr(MODEL, "predict_proba"):
        proba = MODEL.predict_proba(X)[0]
        class_idx = int(np.argmax(proba))
        confidence = float(proba[class_idx])
    else:
        raw = MODEL.predict(X, verbose=0)
        proba = raw[0].tolist()
        class_idx = int(np.argmax(proba))
        confidence = float(proba[class_idx])

    segment = LABEL_ENCODER.classes_[class_idx] if LABEL_ENCODER else str(class_idx)
    return {
        "segment": segment,
        "confidence": round(confidence, 4),
        "probabilities": {
            LABEL_ENCODER.classes_[i]: round(float(p), 4)
            for i, p in enumerate(proba)
        } if LABEL_ENCODER else {"class_" + str(i): round(float(p), 4) for i, p in enumerate(proba)},
    }


# ── Routes ────────────────────────────────────────────────────

@app.get("/health")
def health():
    return jsonify({"status": "ok", "model_loaded": MODEL is not None})


@app.post("/predict")
def predict():
    body = request.get_json(silent=True)
    if not body:
        return jsonify({"error": "Request body must be JSON"}), 400

    if MODEL is None or PREPROCESSOR is None:
        return jsonify({"error": "Model not loaded"}), 503

    cleaned, err = validate_input(body)
    if err:
        return jsonify({"error": err}), 400

    try:
        result = predict_single(cleaned)
        logger.info(f"Prediction: {result['segment']} ({result['confidence']:.4f})")
        return jsonify(result)
    except Exception:
        logger.exception("Prediction error")
        return jsonify({"error": "An internal error occurred during prediction"}), 500


@app.post("/predict_batch")
def predict_batch():
    body = request.get_json(silent=True)
    if not body or not isinstance(body, list):
        return jsonify({"error": "Request body must be a JSON array"}), 400

    if MODEL is None or PREPROCESSOR is None:
        return jsonify({"error": "Model not loaded"}), 503

    results = []
    errors = []
    for i, record in enumerate(body):
        cleaned, err = validate_input(record)
        if err:
            errors.append({"index": i, "error": err})
            results.append(None)
            continue
        try:
            results.append(predict_single(cleaned))
        except Exception:
            logger.exception("Batch prediction error at index %d", i)
            errors.append({"index": i, "error": "An internal error occurred during prediction"})
            results.append(None)

    return jsonify({"predictions": results, "errors": errors, "total": len(body)})


@app.get("/model_info")
def model_info():
    return jsonify(MODEL_INFO or {"message": "Model card not available"})


@app.get("/feature_importance")
def feature_importance():
    return jsonify(FEATURE_IMPORTANCE or {"message": "Feature importance not available"})


@app.get("/model_performance")
def model_performance():
    return jsonify(EVAL_METRICS or {"message": "Evaluation metrics not available"})


# ── Entry point ───────────────────────────────────────────────

if __name__ == "__main__":
    load_model_artifacts()
    port = int(os.environ.get("PORT", 5000))
    debug = os.environ.get("FLASK_DEBUG", "0") == "1"
    app.run(host="0.0.0.0", port=port, debug=debug)
