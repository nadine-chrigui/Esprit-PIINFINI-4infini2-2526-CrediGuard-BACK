"""
Step 9: Model Evaluation
- Load trained models and test data
- Accuracy, Precision, Recall, F1-Score, AUC-ROC
- Confusion matrices
- ROC curves comparison
- Classification reports
- Model comparison visualizations
"""

import json
import pickle
import warnings
from pathlib import Path

import matplotlib
matplotlib.use("Agg")

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns
from sklearn.metrics import (
    ConfusionMatrixDisplay,
    accuracy_score,
    classification_report,
    confusion_matrix,
    f1_score,
    precision_score,
    recall_score,
    roc_auc_score,
    roc_curve,
)
from sklearn.preprocessing import label_binarize

warnings.filterwarnings("ignore")

DATA_DIR = Path(__file__).resolve().parent.parent / "data"
MODELS_DIR = Path(__file__).resolve().parent.parent / "models"
REPORTS_DIR = Path(__file__).resolve().parent.parent / "reports"
SPLITS_FILE = DATA_DIR / "data_splits.pkl"


def load_sklearn_model(name: str):
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


def evaluate_model(name: str, model, X_test, y_test, class_names: list, nn: bool = False):
    if model is None:
        print(f"  [{name}] model file not found – skipping.")
        return None

    if nn:
        import numpy as np
        y_pred = np.argmax(model.predict(X_test, verbose=0), axis=1)
        y_prob = model.predict(X_test, verbose=0)
    else:
        y_pred = model.predict(X_test)
        y_prob = model.predict_proba(X_test) if hasattr(model, "predict_proba") else None

    n_classes = len(class_names)
    acc = accuracy_score(y_test, y_pred)
    prec = precision_score(y_test, y_pred, average="weighted", zero_division=0)
    rec = recall_score(y_test, y_pred, average="weighted", zero_division=0)
    f1 = f1_score(y_test, y_pred, average="weighted", zero_division=0)

    auc = None
    if y_prob is not None and n_classes > 1:
        y_bin = label_binarize(y_test, classes=list(range(n_classes)))
        try:
            auc = roc_auc_score(y_bin, y_prob, average="weighted", multi_class="ovr")
        except Exception:
            pass

    report = classification_report(y_test, y_pred, target_names=class_names)

    metrics = {
        "accuracy": round(acc, 4),
        "precision": round(prec, 4),
        "recall": round(rec, 4),
        "f1_score": round(f1, 4),
        "roc_auc": round(auc, 4) if auc is not None else None,
    }

    print(f"\n[{name}]")
    for k, v in metrics.items():
        print(f"  {k:12s}: {v}")
    print("\nClassification Report:\n", report)

    return {
        "metrics": metrics,
        "y_pred": y_pred,
        "y_prob": y_prob,
        "report": report,
    }


def plot_confusion_matrices(all_results: dict, y_test, class_names: list):
    n = len(all_results)
    fig, axes = plt.subplots(1, n, figsize=(6 * n, 5))
    if n == 1:
        axes = [axes]
    for ax, (name, res) in zip(axes, all_results.items()):
        if res is None:
            continue
        cm = confusion_matrix(y_test, res["y_pred"])
        disp = ConfusionMatrixDisplay(confusion_matrix=cm, display_labels=class_names)
        disp.plot(ax=ax, colorbar=False, cmap="Blues")
        ax.set_title(name, fontsize=12)
    plt.suptitle("Confusion Matrices", fontsize=14)
    plt.tight_layout()
    path = REPORTS_DIR / "06_confusion_matrices.png"
    plt.savefig(path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"  Saved: {path.name}")


def plot_roc_curves(all_results: dict, y_test, class_names: list):
    n_classes = len(class_names)
    y_bin = label_binarize(y_test, classes=list(range(n_classes)))

    fig, ax = plt.subplots(figsize=(9, 6))
    colors = ["steelblue", "darkorange", "forestgreen", "crimson"]

    for (name, res), color in zip(all_results.items(), colors):
        if res is None or res["y_prob"] is None:
            continue
        for i, cls in enumerate(class_names):
            fpr, tpr, _ = roc_curve(y_bin[:, i], res["y_prob"][:, i])
            auc_val = res["metrics"].get("roc_auc", 0) or 0
            ax.plot(fpr, tpr, lw=1.5, alpha=0.7,
                    label=f"{name} – {cls} (AUC={auc_val:.3f})",
                    color=color, linestyle=["solid", "dashed", "dotted"][i % 3])

    ax.plot([0, 1], [0, 1], "k--", lw=1)
    ax.set_xlabel("False Positive Rate")
    ax.set_ylabel("True Positive Rate")
    ax.set_title("ROC Curves – All Models")
    ax.legend(fontsize=7, loc="lower right")
    path = REPORTS_DIR / "07_roc_curves.png"
    plt.tight_layout()
    plt.savefig(path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"  Saved: {path.name}")


def plot_model_comparison(all_results: dict):
    rows = []
    for name, res in all_results.items():
        if res is None:
            continue
        row = {"Model": name}
        row.update(res["metrics"])
        rows.append(row)

    df = pd.DataFrame(rows).set_index("Model")
    metrics = [c for c in ["accuracy", "precision", "recall", "f1_score", "roc_auc"] if c in df.columns]
    df_plot = df[metrics].dropna(axis=1)

    fig, ax = plt.subplots(figsize=(10, 5))
    x = np.arange(len(df_plot))
    width = 0.15
    for i, metric in enumerate(df_plot.columns):
        ax.bar(x + i * width, df_plot[metric].values, width, label=metric)
    ax.set_xticks(x + width * (len(df_plot.columns) - 1) / 2)
    ax.set_xticklabels(df_plot.index, rotation=15)
    ax.set_ylim(0, 1.1)
    ax.set_ylabel("Score")
    ax.set_title("Model Performance Comparison")
    ax.legend(fontsize=9, loc="lower right")
    path = REPORTS_DIR / "08_model_comparison.png"
    plt.tight_layout()
    plt.savefig(path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"  Saved: {path.name}")
    return df


def main():
    print("=" * 60)
    print("Step 9: Model Evaluation")
    print("=" * 60)

    REPORTS_DIR.mkdir(parents=True, exist_ok=True)

    with open(SPLITS_FILE, "rb") as f:
        splits = pickle.load(f)

    X_test = splits["X_test"]
    y_test = splits["y_test"]
    class_names = splits["class_names"]

    print(f"\nTest shape: {X_test.shape}")
    print(f"Classes: {class_names}")

    models = {
        "Logistic Regression": (load_sklearn_model("logistic_regression"), False),
        "Random Forest": (load_sklearn_model("random_forest"), False),
        "XGBoost": (load_sklearn_model("xgboost"), False),
        "Neural Network": (load_nn_model(), True),
    }

    all_results = {}
    for name, (model, is_nn) in models.items():
        all_results[name] = evaluate_model(name, model, X_test, y_test, class_names, nn=is_nn)

    print("\nGenerating visualizations…")
    plot_confusion_matrices(all_results, y_test, class_names)
    plot_roc_curves(all_results, y_test, class_names)
    comparison_df = plot_model_comparison(all_results)

    print("\nModel Comparison:")
    print(comparison_df.to_string())

    # Save JSON metrics
    eval_metrics = {
        name: res["metrics"]
        for name, res in all_results.items()
        if res is not None
    }
    metrics_file = REPORTS_DIR / "evaluation_metrics.json"
    with open(metrics_file, "w") as f:
        json.dump(eval_metrics, f, indent=2)
    print(f"\nMetrics saved to: {metrics_file}")


if __name__ == "__main__":
    main()
