"""
Step 10: Feature Importance & Explainability
- Feature importance from tree-based models
- SHAP values for predictions
- Permutation importance
- Individual prediction explanations
"""

import pickle
import warnings
from pathlib import Path

import matplotlib
matplotlib.use("Agg")

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import shap
from sklearn.inspection import permutation_importance

warnings.filterwarnings("ignore")

DATA_DIR = Path(__file__).resolve().parent.parent / "data"
MODELS_DIR = Path(__file__).resolve().parent.parent / "models"
REPORTS_DIR = Path(__file__).resolve().parent.parent / "reports"
SPLITS_FILE = DATA_DIR / "data_splits.pkl"


def save_fig(name: str, dpi: int = 150):
    plt.tight_layout()
    path = REPORTS_DIR / f"{name}.png"
    plt.savefig(path, dpi=dpi, bbox_inches="tight")
    plt.close()
    print(f"  Saved: {path.name}")


def plot_rf_feature_importance(model, feature_names: list):
    importances = model.feature_importances_
    indices = np.argsort(importances)[::-1][:20]

    fig, ax = plt.subplots(figsize=(10, 6))
    ax.bar(range(len(indices)), importances[indices], color="steelblue", alpha=0.8)
    ax.set_xticks(range(len(indices)))
    ax.set_xticklabels([feature_names[i] for i in indices], rotation=45, ha="right")
    ax.set_title("Random Forest – Feature Importance (Top 20)")
    ax.set_ylabel("Importance")
    save_fig("09_rf_feature_importance")

    return pd.Series(importances, index=feature_names).sort_values(ascending=False)


def plot_xgb_feature_importance(model, feature_names: list):
    importances = model.feature_importances_
    indices = np.argsort(importances)[::-1][:20]

    fig, ax = plt.subplots(figsize=(10, 6))
    ax.bar(range(len(indices)), importances[indices], color="darkorange", alpha=0.8)
    ax.set_xticks(range(len(indices)))
    ax.set_xticklabels([feature_names[i] for i in indices], rotation=45, ha="right")
    ax.set_title("XGBoost – Feature Importance (Top 20)")
    ax.set_ylabel("Importance")
    save_fig("10_xgb_feature_importance")

    return pd.Series(importances, index=feature_names).sort_values(ascending=False)


def compute_shap_values(model, X_test: np.ndarray, feature_names: list, model_name: str, n_samples: int = 200):
    """Compute and plot SHAP summary."""
    X_sample = X_test[:n_samples]
    explainer = shap.TreeExplainer(model)
    shap_values = explainer.shap_values(X_sample)

    # For multi-class: shap_values is a list of arrays
    if isinstance(shap_values, list):
        # Use class with highest mean absolute SHAP
        mean_abs = [np.abs(sv).mean() for sv in shap_values]
        cls_idx = int(np.argmax(mean_abs))
        sv_plot = shap_values[cls_idx]
    else:
        sv_plot = shap_values

    fig, ax = plt.subplots(figsize=(10, 7))
    shap.summary_plot(sv_plot, X_sample, feature_names=feature_names, show=False)
    plt.title(f"SHAP Summary – {model_name}")
    save_fig(f"11_shap_{model_name.lower().replace(' ', '_')}")

    return shap_values


def plot_permutation_importance(model, X_test, y_test, feature_names: list, model_name: str):
    result = permutation_importance(
        model, X_test, y_test,
        n_repeats=10,
        random_state=42,
        scoring="f1_weighted",
    )
    sorted_idx = result.importances_mean.argsort()[::-1][:20]

    fig, ax = plt.subplots(figsize=(10, 6))
    ax.bar(
        range(len(sorted_idx)),
        result.importances_mean[sorted_idx],
        yerr=result.importances_std[sorted_idx],
        color="forestgreen", alpha=0.8,
    )
    ax.set_xticks(range(len(sorted_idx)))
    ax.set_xticklabels([feature_names[i] for i in sorted_idx], rotation=45, ha="right")
    ax.set_title(f"Permutation Importance – {model_name} (Top 20)")
    ax.set_ylabel("Mean Decrease in F1")
    save_fig(f"12_perm_importance_{model_name.lower().replace(' ', '_')}")


def main():
    print("=" * 60)
    print("Step 10: Explainability")
    print("=" * 60)

    REPORTS_DIR.mkdir(parents=True, exist_ok=True)

    with open(SPLITS_FILE, "rb") as f:
        splits = pickle.load(f)

    X_test = splits["X_test"]
    y_test = splits["y_test"]
    raw_feature_names = splits["feature_names"]

    # Build full feature names after one-hot encoding
    preprocessor_path = MODELS_DIR / "preprocessor.pkl"
    if preprocessor_path.exists():
        with open(preprocessor_path, "rb") as f:
            prep_data = pickle.load(f)
        preprocessor = prep_data["preprocessor"]
        try:
            feature_names = list(preprocessor.get_feature_names_out())
        except Exception:
            feature_names = raw_feature_names
    else:
        feature_names = raw_feature_names

    # Adjust feature names length to match X_test columns
    if len(feature_names) != X_test.shape[1]:
        feature_names = [f"feature_{i}" for i in range(X_test.shape[1])]

    # ── Random Forest ───────────────────────────────────────
    rf_path = MODELS_DIR / "random_forest.pkl"
    if rf_path.exists():
        print("\n[Random Forest] Feature Importance & SHAP")
        with open(rf_path, "rb") as f:
            rf_model = pickle.load(f)
        rf_importance = plot_rf_feature_importance(rf_model, feature_names)
        print("  Top 10 features:")
        print(rf_importance.head(10).to_string())
        try:
            compute_shap_values(rf_model, X_test, feature_names, "Random Forest")
        except Exception as e:
            print(f"  SHAP skipped: {e}")
        plot_permutation_importance(rf_model, X_test, y_test, feature_names, "Random Forest")

    # ── XGBoost ─────────────────────────────────────────────
    xgb_path = MODELS_DIR / "xgboost.pkl"
    if xgb_path.exists():
        print("\n[XGBoost] Feature Importance & SHAP")
        with open(xgb_path, "rb") as f:
            xgb_model = pickle.load(f)
        xgb_importance = plot_xgb_feature_importance(xgb_model, feature_names)
        print("  Top 10 features:")
        print(xgb_importance.head(10).to_string())
        try:
            compute_shap_values(xgb_model, X_test, feature_names, "XGBoost")
        except Exception as e:
            print(f"  SHAP skipped: {e}")

    print("\nExplainability analysis complete. Plots saved to reports/")


if __name__ == "__main__":
    main()
