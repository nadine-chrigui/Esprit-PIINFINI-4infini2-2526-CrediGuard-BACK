"""
Step 4: Exploratory Data Analysis
- Distribution analysis with histograms and KDE plots
- Correlation heatmap
- Segment-wise statistics
- Feature relationships (box plots, scatter plots)
- Save visualizations and statistical summaries
"""

from pathlib import Path

import matplotlib
matplotlib.use("Agg")  # non-interactive backend

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns

DATA_FILE = Path(__file__).resolve().parent.parent / "data" / "customer_data_processed.csv"
REPORTS_DIR = Path(__file__).resolve().parent.parent / "reports"

NUMERICAL_FEATURES = [
    "age", "income", "premium_amount", "policy_tenure_months",
    "num_missed_payments_12m", "avg_payment_delay_days",
    "claims_frequency", "credit_score", "payment_consistency_score",
    "account_age_months", "premium_to_income_ratio",
    "payment_risk_score", "tenure_to_age_ratio",
]

TARGET = "segment"
PALETTE = {"Low Risk": "#2ecc71", "Medium Risk": "#f39c12", "High Risk": "#e74c3c"}


def save_fig(name: str, dpi: int = 150):
    plt.tight_layout()
    path = REPORTS_DIR / f"{name}.png"
    plt.savefig(path, dpi=dpi, bbox_inches="tight")
    plt.close()
    print(f"  Saved: {path.name}")


def plot_class_distribution(df: pd.DataFrame):
    counts = df[TARGET].value_counts()
    fig, ax = plt.subplots(figsize=(7, 4))
    bars = ax.bar(counts.index, counts.values,
                  color=[PALETTE[c] for c in counts.index])
    for bar, val in zip(bars, counts.values):
        ax.text(bar.get_x() + bar.get_width() / 2, bar.get_height() + 20,
                str(val), ha="center", fontsize=11)
    ax.set_title("Customer Segment Distribution", fontsize=14)
    ax.set_xlabel("Segment")
    ax.set_ylabel("Count")
    save_fig("01_class_distribution")


def plot_feature_distributions(df: pd.DataFrame, features: list):
    n_cols = 3
    n_rows = int(np.ceil(len(features) / n_cols))
    fig, axes = plt.subplots(n_rows, n_cols, figsize=(18, n_rows * 4))
    axes = axes.flatten()
    for i, feat in enumerate(features):
        for seg in df[TARGET].unique():
            subset = df[df[TARGET] == seg][feat].dropna()
            axes[i].hist(subset, bins=30, alpha=0.5,
                         label=seg, color=PALETTE.get(seg, "gray"))
        axes[i].set_title(feat, fontsize=11)
        axes[i].legend(fontsize=8)
    for j in range(i + 1, len(axes)):
        axes[j].set_visible(False)
    fig.suptitle("Feature Distributions by Segment", fontsize=15, y=1.01)
    save_fig("02_feature_distributions")


def plot_correlation_heatmap(df: pd.DataFrame, features: list):
    corr = df[features].corr()
    fig, ax = plt.subplots(figsize=(14, 11))
    mask = np.triu(np.ones_like(corr, dtype=bool))
    sns.heatmap(corr, mask=mask, annot=True, fmt=".2f",
                cmap="coolwarm", center=0, ax=ax,
                annot_kws={"size": 8})
    ax.set_title("Feature Correlation Heatmap", fontsize=14)
    save_fig("03_correlation_heatmap")


def plot_box_plots(df: pd.DataFrame, features: list):
    key_features = [
        "num_missed_payments_12m", "avg_payment_delay_days",
        "credit_score", "payment_consistency_score",
        "payment_risk_score", "income",
    ]
    key_features = [f for f in key_features if f in features]
    n_cols = 3
    n_rows = int(np.ceil(len(key_features) / n_cols))
    fig, axes = plt.subplots(n_rows, n_cols, figsize=(16, n_rows * 4))
    axes = axes.flatten()
    order = ["Low Risk", "Medium Risk", "High Risk"]
    order = [o for o in order if o in df[TARGET].unique()]
    for i, feat in enumerate(key_features):
        data_by_seg = [df[df[TARGET] == seg][feat].dropna().values for seg in order]
        bp = axes[i].boxplot(data_by_seg, tick_labels=order, patch_artist=True, notch=False)
        for patch, seg in zip(bp["boxes"], order):
            patch.set_facecolor(PALETTE.get(seg, "gray"))
            patch.set_alpha(0.7)
        axes[i].set_title(feat, fontsize=11)
        axes[i].tick_params(axis="x", rotation=15)
    for j in range(i + 1, len(axes)):
        axes[j].set_visible(False)
    fig.suptitle("Box Plots of Key Features by Segment", fontsize=14, y=1.02)
    save_fig("04_box_plots")


def plot_scatter_matrix(df: pd.DataFrame):
    scatter_features = [
        "credit_score", "num_missed_payments_12m",
        "avg_payment_delay_days", "payment_consistency_score",
    ]
    scatter_features = [f for f in scatter_features if f in df.columns]
    subset = df[scatter_features + [TARGET]].dropna()
    color_map = {seg: PALETTE.get(seg, "gray") for seg in subset[TARGET].unique()}
    colors = subset[TARGET].map(color_map)
    pd.plotting.scatter_matrix(
        subset[scatter_features],
        figsize=(12, 10),
        alpha=0.3,
        c=colors,
        diagonal="kde",
    )
    plt.suptitle("Scatter Matrix – Key Risk Features", fontsize=13, y=1.01)
    save_fig("05_scatter_matrix")


def compute_segment_statistics(df: pd.DataFrame, features: list) -> pd.DataFrame:
    stats = df.groupby(TARGET)[features].agg(["mean", "median", "std"])
    stats.columns = ["_".join(col) for col in stats.columns]
    return stats


def main():
    print("=" * 60)
    print("Step 4: Exploratory Data Analysis")
    print("=" * 60)

    REPORTS_DIR.mkdir(parents=True, exist_ok=True)

    print(f"\nLoading data from {DATA_FILE}…")
    df = pd.read_csv(DATA_FILE)
    print(f"Shape: {df.shape}")

    available_num = [f for f in NUMERICAL_FEATURES if f in df.columns]

    print("\nGenerating visualizations…")
    plot_class_distribution(df)
    plot_feature_distributions(df, available_num)
    plot_correlation_heatmap(df, available_num)
    plot_box_plots(df, available_num)
    plot_scatter_matrix(df)

    print("\nSegment-wise statistics:")
    stats = compute_segment_statistics(df, available_num)
    stats_path = REPORTS_DIR / "segment_statistics.csv"
    stats.to_csv(stats_path)
    print(stats.T.head(20).to_string())
    print(f"\nStatistics saved to: {stats_path}")

    print("\nEDA complete. All plots saved to reports/")


if __name__ == "__main__":
    main()
