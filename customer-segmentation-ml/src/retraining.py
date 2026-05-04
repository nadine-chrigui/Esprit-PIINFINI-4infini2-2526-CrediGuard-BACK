"""
Step 13 (continued): Automated Retraining Pipeline
- Load new data since last training
- Preprocess new data
- Retrain models with accumulated data
- Compare with current production model
- Update production model if improvement detected
- Log retraining history
"""

import json
import logging
import pickle
import time
import warnings
from datetime import datetime
from pathlib import Path

import joblib
import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, f1_score
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder

from monitoring import simulate_new_data  # noqa: E402

warnings.filterwarnings("ignore")

BASE_DIR = Path(__file__).resolve().parent.parent
DATA_DIR = BASE_DIR / "data"
MODELS_DIR = BASE_DIR / "models"
REPORTS_DIR = BASE_DIR / "reports"
RETRAIN_LOG = BASE_DIR / "logs" / "retraining_history.json"

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)

RANDOM_STATE = 42
MIN_IMPROVEMENT = 0.005  # Require ≥0.5% F1 improvement to swap production model

# Simulation parameters
RETRAIN_N_SAMPLES = 1000
RETRAIN_SHIFT = 0.5   # multiplier for mean shift in simulated retraining data distributions


# ─────────────────────────────────────────────────────────────
# Helpers
# ─────────────────────────────────────────────────────────────

def assign_segment(row: pd.Series) -> str:
    if row["num_missed_payments_12m"] >= 3 or row["avg_payment_delay_days"] > 30:
        return "High Risk"
    if row["num_missed_payments_12m"] >= 1 or row["avg_payment_delay_days"] > 15:
        return "Medium Risk"
    return "Low Risk"


def engineer_features(df: pd.DataFrame) -> pd.DataFrame:
    df = df.copy()
    df["premium_to_income_ratio"] = (df["premium_amount"] / df["income"].replace(0, np.nan)).round(4).fillna(0)
    df["payment_risk_score"] = (
        df["num_missed_payments_12m"] * 10
        + df["avg_payment_delay_days"] * 0.5
        + (1 - df["payment_consistency_score"]) * 20
    ).round(2)
    df["tenure_to_age_ratio"] = (df["policy_tenure_months"] / (df["age"] * 12)).round(4)
    return df


def load_current_model():
    info_path = MODELS_DIR / "best_model_info.json"
    if not info_path.exists():
        return None, None
    with open(info_path) as f:
        info = json.load(f)
    if info.get("is_nn"):
        import tensorflow as tf
        path = MODELS_DIR / "best_model.keras"
        if path.exists():
            return tf.keras.models.load_model(str(path)), info
    else:
        path = MODELS_DIR / "best_model.pkl"
        if path.exists():
            return joblib.load(path), info
    return None, None


def load_retraining_history() -> list:
    if not RETRAIN_LOG.exists():
        return []
    with open(RETRAIN_LOG) as f:
        return json.load(f)


def save_retraining_history(history: list):
    RETRAIN_LOG.parent.mkdir(parents=True, exist_ok=True)
    with open(RETRAIN_LOG, "w") as f:
        json.dump(history, f, indent=2)


# ─────────────────────────────────────────────────────────────
# Retraining
# ─────────────────────────────────────────────────────────────

def retrain_with_new_data(new_df: pd.DataFrame) -> tuple:
    """Retrain a Random Forest on original + new data. Returns (model, metrics)."""

    # Load original data
    orig_csv = DATA_DIR / "customer_data_processed.csv"
    if orig_csv.exists():
        orig_df = pd.read_csv(orig_csv)
        # Make sure new_df has a segment column
        if "segment" not in new_df.columns:
            new_df = new_df.copy()
            new_df["segment"] = new_df.apply(assign_segment, axis=1)
        combined = pd.concat([orig_df, new_df], ignore_index=True)
    else:
        if "segment" not in new_df.columns:
            new_df = new_df.copy()
            new_df["segment"] = new_df.apply(assign_segment, axis=1)
        combined = new_df

    combined = engineer_features(combined)

    feature_cols = [
        "age", "income", "premium_amount", "policy_tenure_months",
        "num_missed_payments_12m", "avg_payment_delay_days", "claims_frequency",
        "credit_score", "payment_consistency_score", "account_age_months",
        "premium_to_income_ratio", "payment_risk_score", "tenure_to_age_ratio",
    ]
    feature_cols = [c for c in feature_cols if c in combined.columns]

    le = LabelEncoder()
    y = le.fit_transform(combined["segment"])
    X = combined[feature_cols].fillna(0).values

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.3, stratify=y, random_state=RANDOM_STATE
    )

    model = RandomForestClassifier(n_estimators=200, random_state=RANDOM_STATE, n_jobs=-1)
    start = time.time()
    model.fit(X_train, y_train)
    elapsed = time.time() - start

    y_pred = model.predict(X_test)
    acc = accuracy_score(y_test, y_pred)
    f1 = f1_score(y_test, y_pred, average="weighted", zero_division=0)

    metrics = {
        "accuracy": round(acc, 4),
        "f1_weighted": round(f1, 4),
        "training_samples": len(X_train),
        "training_time_sec": round(elapsed, 2),
    }

    return model, metrics, le


def evaluate_current_model(model, splits_path: Path) -> dict:
    if not splits_path.exists():
        return {}
    with open(splits_path, "rb") as f:
        splits = pickle.load(f)
    X_test = splits["X_test"]
    y_test = splits["y_test"]

    is_nn = not hasattr(model, "predict_proba")
    if is_nn:
        y_pred = np.argmax(model.predict(X_test, verbose=0), axis=1)
    else:
        y_pred = model.predict(X_test)

    return {
        "accuracy": round(accuracy_score(y_test, y_pred), 4),
        "f1_weighted": round(f1_score(y_test, y_pred, average="weighted", zero_division=0), 4),
    }


# ─────────────────────────────────────────────────────────────
# Main
# ─────────────────────────────────────────────────────────────

def main():
    print("=" * 60)
    print("Automated Retraining Pipeline")
    print("=" * 60)

    MODELS_DIR.mkdir(parents=True, exist_ok=True)
    REPORTS_DIR.mkdir(parents=True, exist_ok=True)

    # Simulate new data arriving
    print("\nLoading new data (simulated)…")
    new_df = simulate_new_data(n=RETRAIN_N_SAMPLES, shift=RETRAIN_SHIFT)
    print(f"  New data shape: {new_df.shape}")

    # Load current production model
    current_model, current_info = load_current_model()
    splits_path = DATA_DIR / "data_splits.pkl"

    current_metrics = {}
    if current_model is not None and splits_path.exists():
        current_metrics = evaluate_current_model(current_model, splits_path)
        print(f"\nCurrent model metrics:  {current_metrics}")
    else:
        print("\nNo current model found or data splits missing.")

    # Retrain
    print("\nRetraining model with accumulated data…")
    new_model, new_metrics, new_le = retrain_with_new_data(new_df)
    print(f"New model metrics: {new_metrics}")

    # Compare
    current_f1 = current_metrics.get("f1_weighted", 0.0)
    new_f1 = new_metrics["f1_weighted"]
    improvement = new_f1 - current_f1

    print(f"\nImprovement in F1: {improvement:+.4f} (threshold={MIN_IMPROVEMENT})")

    if improvement >= MIN_IMPROVEMENT or current_model is None:
        print("✓ New model is better. Updating production model…")
        model_path = MODELS_DIR / "best_model.pkl"
        joblib.dump(new_model, model_path)
        with open(MODELS_DIR / "best_model_info.json", "w") as f:
            json.dump({"name": "random_forest_retrained", "is_nn": False}, f)
        print(f"  Production model updated: {model_path}")
        updated = True
    else:
        print("✗ New model not significantly better. Keeping current model.")
        updated = False

    # Log retraining history
    history = load_retraining_history()
    history.append({
        "timestamp": datetime.now().isoformat(),
        "new_data_samples": len(new_df),
        "new_model_metrics": new_metrics,
        "current_model_metrics": current_metrics,
        "improvement": round(improvement, 4),
        "model_updated": updated,
    })
    save_retraining_history(history)
    print(f"\nRetraining history saved to: {RETRAIN_LOG}")


if __name__ == "__main__":
    main()
