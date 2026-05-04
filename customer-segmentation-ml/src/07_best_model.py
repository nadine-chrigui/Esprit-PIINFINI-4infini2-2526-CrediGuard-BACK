"""
Step 11: Model Validation & Best Model Selection
- Compare all models based on evaluation metrics
- Select best performing model
- Save serialized best model (joblib/pickle)
- Save preprocessor and scaler
- Create model card with metadata
"""

import json
import pickle
from datetime import datetime
from pathlib import Path

import joblib
import numpy as np
from sklearn.metrics import accuracy_score, f1_score

MODELS_DIR = Path(__file__).resolve().parent.parent / "models"
DATA_DIR = Path(__file__).resolve().parent.parent / "data"
REPORTS_DIR = Path(__file__).resolve().parent.parent / "reports"
SPLITS_FILE = DATA_DIR / "data_splits.pkl"


def load_model(name: str):
    path = MODELS_DIR / f"{name}.pkl"
    if not path.exists():
        return None
    with open(path, "rb") as f:
        return pickle.load(f)


def load_nn_model():
    import tensorflow as tf
    path = MODELS_DIR / "neural_network.keras"
    if not path.exists():
        return None
    return tf.keras.models.load_model(str(path))


def main():
    print("=" * 60)
    print("Step 11: Best Model Selection")
    print("=" * 60)

    REPORTS_DIR.mkdir(parents=True, exist_ok=True)
    MODELS_DIR.mkdir(parents=True, exist_ok=True)

    with open(SPLITS_FILE, "rb") as f:
        splits = pickle.load(f)

    X_test = splits["X_test"]
    y_test = splits["y_test"]
    class_names = splits["class_names"]

    candidates = {
        "logistic_regression": (load_model("logistic_regression"), False),
        "random_forest": (load_model("random_forest"), False),
        "xgboost": (load_model("xgboost"), False),
        "neural_network": (load_nn_model(), True),
    }

    scores = {}
    for name, (model, is_nn) in candidates.items():
        if model is None:
            print(f"  [{name}] not found – skipping.")
            continue
        if is_nn:
            y_pred = np.argmax(model.predict(X_test, verbose=0), axis=1)
        else:
            y_pred = model.predict(X_test)

        acc = accuracy_score(y_test, y_pred)
        f1 = f1_score(y_test, y_pred, average="weighted", zero_division=0)
        scores[name] = {"model": model, "is_nn": is_nn, "accuracy": acc, "f1_weighted": f1}
        print(f"  {name:25s}  Acc={acc:.4f}  F1={f1:.4f}")

    if not scores:
        print("No models found. Run 04_model_training.py first.")
        return

    # Select best by F1 weighted
    best_name = max(scores, key=lambda n: scores[n]["f1_weighted"])
    best_model = scores[best_name]["model"]
    best_is_nn = scores[best_name]["is_nn"]

    print(f"\nBest model: {best_name}")
    print(f"  Accuracy:    {scores[best_name]['accuracy']:.4f}")
    print(f"  F1 weighted: {scores[best_name]['f1_weighted']:.4f}")

    # Save best model
    if best_is_nn:
        best_model.save(str(MODELS_DIR / "best_model.keras"))
        print(f"\nBest model saved to: {MODELS_DIR}/best_model.keras")
    else:
        joblib.dump(best_model, MODELS_DIR / "best_model.pkl")
        print(f"\nBest model saved to: {MODELS_DIR}/best_model.pkl")

    # Save model card
    model_card = {
        "model_name": best_name,
        "model_type": "neural_network" if best_is_nn else "sklearn",
        "training_date": datetime.now().isoformat(),
        "version": "1.0.0",
        "performance": {
            "accuracy": scores[best_name]["accuracy"],
            "f1_weighted": scores[best_name]["f1_weighted"],
        },
        "classes": class_names,
        "all_models": {
            n: {"accuracy": v["accuracy"], "f1_weighted": v["f1_weighted"]}
            for n, v in scores.items()
        },
        "task": "multi-class classification",
        "description": "Customer risk segmentation: Low Risk, Medium Risk, High Risk",
    }

    card_file = MODELS_DIR / "model_card.json"
    with open(card_file, "w") as f:
        json.dump(model_card, f, indent=2)
    print(f"Model card saved to: {card_file}")

    # Save best model name marker
    with open(MODELS_DIR / "best_model_info.json", "w") as f:
        json.dump({"name": best_name, "is_nn": best_is_nn}, f)


if __name__ == "__main__":
    main()
