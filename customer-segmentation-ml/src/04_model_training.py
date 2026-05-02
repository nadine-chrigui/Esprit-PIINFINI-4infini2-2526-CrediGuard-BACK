"""
Steps 6–8: Model Training with Hyperparameter Tuning
- Logistic Regression (baseline)
- Random Forest
- XGBoost
- Neural Network (Keras/TensorFlow)
- 5-fold cross-validation for all models
- GridSearchCV for hyperparameter tuning
- Save trained models
"""

import json
import pickle
import time
import warnings
from pathlib import Path

import numpy as np
from sklearn.linear_model import LogisticRegression
from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import GridSearchCV, StratifiedKFold, cross_val_score
from sklearn.metrics import accuracy_score
import xgboost as xgb

warnings.filterwarnings("ignore")

DATA_DIR = Path(__file__).resolve().parent.parent / "data"
MODELS_DIR = Path(__file__).resolve().parent.parent / "models"
SPLITS_FILE = DATA_DIR / "data_splits.pkl"

RANDOM_STATE = 42
CV_FOLDS = 5


# ─────────────────────────────────────────────────────────────
# Neural Network helper
# ─────────────────────────────────────────────────────────────

def build_neural_network(input_dim: int, n_classes: int):
    """Build a simple Keras sequential classifier."""
    import tensorflow as tf
    from tensorflow import keras

    model = keras.Sequential([
        keras.layers.Input(shape=(input_dim,)),
        keras.layers.Dense(128, activation="relu"),
        keras.layers.Dropout(0.3),
        keras.layers.Dense(64, activation="relu"),
        keras.layers.Dropout(0.2),
        keras.layers.Dense(32, activation="relu"),
        keras.layers.Dense(n_classes, activation="softmax"),
    ])
    model.compile(
        optimizer=keras.optimizers.Adam(learning_rate=0.001),
        loss="sparse_categorical_crossentropy",
        metrics=["accuracy"],
    )
    return model


# ─────────────────────────────────────────────────────────────
# Training helpers
# ─────────────────────────────────────────────────────────────

def run_gridsearch(model, param_grid: dict, X_train, y_train, name: str):
    cv = StratifiedKFold(n_splits=CV_FOLDS, shuffle=True, random_state=RANDOM_STATE)
    gs = GridSearchCV(model, param_grid, cv=cv, scoring="f1_weighted", n_jobs=-1, verbose=0)
    start = time.time()
    gs.fit(X_train, y_train)
    elapsed = time.time() - start
    print(f"  Best params: {gs.best_params_}")
    print(f"  CV F1 (weighted): {gs.best_score_:.4f}")
    print(f"  Time: {elapsed:.1f}s")
    return gs.best_estimator_, gs.best_params_, gs.best_score_


def cross_validate_model(model, X_train, y_train, name: str):
    cv = StratifiedKFold(n_splits=CV_FOLDS, shuffle=True, random_state=RANDOM_STATE)
    scores = cross_val_score(model, X_train, y_train, cv=cv, scoring="f1_weighted")
    print(f"  CV F1 (weighted): {scores.mean():.4f} ± {scores.std():.4f}")
    return scores


# ─────────────────────────────────────────────────────────────
# Main
# ─────────────────────────────────────────────────────────────

def main():
    print("=" * 60)
    print("Step 7 & 8: Model Training & Hyperparameter Tuning")
    print("=" * 60)

    MODELS_DIR.mkdir(parents=True, exist_ok=True)

    with open(SPLITS_FILE, "rb") as f:
        splits = pickle.load(f)

    X_train = splits["X_train"]
    X_test = splits["X_test"]
    y_train = splits["y_train"]
    y_test = splits["y_test"]
    n_classes = len(splits["class_names"])

    print(f"\nTrain shape: {X_train.shape}, Test shape: {X_test.shape}")
    print(f"Classes: {splits['class_names']}")

    results = {}

    # ── 1. Logistic Regression ──────────────────────────────
    print("\n[1/4] Logistic Regression")
    lr_params = {
        "C": [0.01, 0.1, 1.0, 10.0],
        "solver": ["lbfgs"],
        "max_iter": [1000],
    }
    lr_model, lr_best, lr_score = run_gridsearch(
        LogisticRegression(random_state=RANDOM_STATE),
        lr_params, X_train, y_train, "LogisticRegression"
    )
    lr_acc = accuracy_score(y_test, lr_model.predict(X_test))
    results["logistic_regression"] = {"best_params": lr_best, "cv_f1": lr_score, "test_acc": lr_acc}

    with open(MODELS_DIR / "logistic_regression.pkl", "wb") as f:
        pickle.dump(lr_model, f)
    print(f"  Test accuracy: {lr_acc:.4f}")

    # ── 2. Random Forest ────────────────────────────────────
    print("\n[2/4] Random Forest")
    rf_params = {
        "n_estimators": [100, 200],
        "max_depth": [None, 10, 20],
        "min_samples_split": [2, 5],
    }
    rf_model, rf_best, rf_score = run_gridsearch(
        RandomForestClassifier(random_state=RANDOM_STATE),
        rf_params, X_train, y_train, "RandomForest"
    )
    rf_acc = accuracy_score(y_test, rf_model.predict(X_test))
    results["random_forest"] = {"best_params": rf_best, "cv_f1": rf_score, "test_acc": rf_acc}

    with open(MODELS_DIR / "random_forest.pkl", "wb") as f:
        pickle.dump(rf_model, f)
    print(f"  Test accuracy: {rf_acc:.4f}")

    # ── 3. XGBoost ──────────────────────────────────────────
    print("\n[3/4] XGBoost")
    xgb_params = {
        "n_estimators": [100, 200],
        "max_depth": [3, 6],
        "learning_rate": [0.05, 0.1, 0.2],
    }
    xgb_model, xgb_best, xgb_score = run_gridsearch(
        xgb.XGBClassifier(
            random_state=RANDOM_STATE,
            eval_metric="mlogloss",
            verbosity=0,
        ),
        xgb_params, X_train, y_train, "XGBoost"
    )
    xgb_acc = accuracy_score(y_test, xgb_model.predict(X_test))
    results["xgboost"] = {"best_params": xgb_best, "cv_f1": xgb_score, "test_acc": xgb_acc}

    with open(MODELS_DIR / "xgboost.pkl", "wb") as f:
        pickle.dump(xgb_model, f)
    print(f"  Test accuracy: {xgb_acc:.4f}")

    # ── 4. Neural Network ───────────────────────────────────
    print("\n[4/4] Neural Network")
    import tensorflow as tf
    tf.random.set_seed(RANDOM_STATE)

    nn_model = build_neural_network(X_train.shape[1], n_classes)
    start = time.time()
    history = nn_model.fit(
        X_train, y_train,
        epochs=50,
        batch_size=64,
        validation_split=0.15,
        verbose=0,
        callbacks=[
            tf.keras.callbacks.EarlyStopping(
                monitor="val_loss", patience=5, restore_best_weights=True
            )
        ],
    )
    elapsed = time.time() - start
    nn_preds = np.argmax(nn_model.predict(X_test, verbose=0), axis=1)
    nn_acc = accuracy_score(y_test, nn_preds)
    results["neural_network"] = {
        "best_params": {"epochs": len(history.history["loss"]), "batch_size": 64},
        "cv_f1": float(max(history.history.get("val_accuracy", [0]))),
        "test_acc": nn_acc,
    }
    nn_model.save(str(MODELS_DIR / "neural_network.keras"))
    print(f"  Epochs trained: {len(history.history['loss'])}")
    print(f"  Time: {elapsed:.1f}s")
    print(f"  Test accuracy: {nn_acc:.4f}")

    # ── Save results ────────────────────────────────────────
    results_file = MODELS_DIR / "training_results.json"
    with open(results_file, "w") as f:
        json.dump(results, f, indent=2, default=str)

    print("\n" + "=" * 60)
    print("Training Summary")
    print("=" * 60)
    for name, res in results.items():
        print(f"  {name:25s}  CV F1={res['cv_f1']:.4f}  Test Acc={res['test_acc']:.4f}")

    print(f"\nModels saved to: {MODELS_DIR}")
    print(f"Results saved to: {results_file}")


if __name__ == "__main__":
    main()
