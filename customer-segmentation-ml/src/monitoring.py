"""
Step 13: Model Monitoring & Drift Detection
- Track predictions over time
- Detect data drift (compare distributions)
- Monitor model performance degradation
- Generate performance reports
- Alert on suspicious patterns
"""

import json
import logging
import warnings
from datetime import datetime, timedelta
from pathlib import Path

import matplotlib
matplotlib.use("Agg")

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
from scipy import stats

warnings.filterwarnings("ignore")

BASE_DIR = Path(__file__).resolve().parent.parent
DATA_DIR = BASE_DIR / "data"
MODELS_DIR = BASE_DIR / "models"
REPORTS_DIR = BASE_DIR / "reports"
MONITORING_LOG = BASE_DIR / "logs" / "predictions.jsonl"

logging.basicConfig(level=logging.INFO, format="%(asctime)s %(levelname)s %(message)s")
logger = logging.getLogger(__name__)

NUMERICAL_FEATURES = [
    "age", "income", "premium_amount", "policy_tenure_months",
    "num_missed_payments_12m", "avg_payment_delay_days",
    "claims_frequency", "credit_score", "payment_consistency_score",
    "account_age_months",
]

DRIFT_THRESHOLD_PSI = 0.2   # Population Stability Index threshold
DRIFT_THRESHOLD_KS = 0.05   # KS-test p-value threshold (reject if below)
PERF_DEGRADATION_THRESHOLD = 0.05  # 5% drop in accuracy triggers alert

# Simulation parameters
SIMULATION_N_SAMPLES = 500
SIMULATION_SHIFT = 1.0   # multiplier for mean shift in simulated data distributions
SIMULATION_SEED = 99


# ─────────────────────────────────────────────────────────────
# PSI (Population Stability Index)
# ─────────────────────────────────────────────────────────────

def compute_psi(expected: np.ndarray, actual: np.ndarray, n_bins: int = 10) -> float:
    """Compute PSI between expected (training) and actual (new) distributions."""
    bins = np.percentile(expected, np.linspace(0, 100, n_bins + 1))
    bins = np.unique(bins)
    if len(bins) < 2:
        return 0.0

    exp_counts, _ = np.histogram(expected, bins=bins)
    act_counts, _ = np.histogram(actual, bins=bins)

    exp_pct = (exp_counts + 1e-6) / len(expected)
    act_pct = (act_counts + 1e-6) / len(actual)

    psi = np.sum((act_pct - exp_pct) * np.log(act_pct / exp_pct))
    return float(psi)


# ─────────────────────────────────────────────────────────────
# Drift detection
# ─────────────────────────────────────────────────────────────

def detect_drift(train_df: pd.DataFrame, new_df: pd.DataFrame) -> dict:
    """Detect feature drift using KS test and PSI."""
    results = {}
    for col in NUMERICAL_FEATURES:
        if col not in train_df.columns or col not in new_df.columns:
            continue
        ks_stat, ks_pval = stats.ks_2samp(
            train_df[col].dropna().values,
            new_df[col].dropna().values,
        )
        psi = compute_psi(
            train_df[col].dropna().values,
            new_df[col].dropna().values,
        )
        drifted = (ks_pval < DRIFT_THRESHOLD_KS) or (psi > DRIFT_THRESHOLD_PSI)
        results[col] = {
            "ks_statistic": round(ks_stat, 4),
            "ks_pvalue": round(ks_pval, 4),
            "psi": round(psi, 4),
            "drift_detected": drifted,
        }
    return results


# ─────────────────────────────────────────────────────────────
# Prediction tracking
# ─────────────────────────────────────────────────────────────

def log_prediction(record: dict):
    """Append a prediction record to the JSONL log.

    Args:
        record: Dictionary containing prediction data to log. Common keys include
            'segment' (predicted class), 'confidence' (float 0–1), and any input
            feature values to track. A 'timestamp' key is automatically added.
    """
    MONITORING_LOG.parent.mkdir(parents=True, exist_ok=True)
    record["timestamp"] = datetime.now().isoformat()
    with open(MONITORING_LOG, "a") as f:
        f.write(json.dumps(record) + "\n")


def load_prediction_log() -> pd.DataFrame:
    if not MONITORING_LOG.exists():
        return pd.DataFrame()
    records = []
    with open(MONITORING_LOG) as f:
        for line in f:
            try:
                records.append(json.loads(line))
            except json.JSONDecodeError:
                pass
    return pd.DataFrame(records)


# ─────────────────────────────────────────────────────────────
# Performance monitoring
# ─────────────────────────────────────────────────────────────

def monitor_prediction_distribution(log_df: pd.DataFrame):
    """Plot prediction label distribution over time."""
    if log_df.empty or "segment" not in log_df.columns:
        print("  No prediction log found.")
        return
    counts = log_df["segment"].value_counts()
    fig, ax = plt.subplots(figsize=(7, 4))
    ax.bar(counts.index, counts.values, color=["#2ecc71", "#f39c12", "#e74c3c"])
    ax.set_title("Prediction Distribution")
    ax.set_ylabel("Count")
    plt.tight_layout()
    path = REPORTS_DIR / "13_prediction_distribution.png"
    plt.savefig(path, dpi=150, bbox_inches="tight")
    plt.close()
    print(f"  Saved: {path.name}")


def generate_monitoring_report(drift_results: dict, log_df: pd.DataFrame) -> dict:
    drifted_features = [f for f, v in drift_results.items() if v["drift_detected"]]
    report = {
        "report_timestamp": datetime.now().isoformat(),
        "total_predictions_logged": len(log_df),
        "drift_summary": {
            "features_checked": len(drift_results),
            "features_drifted": len(drifted_features),
            "drifted_features": drifted_features,
        },
        "drift_details": drift_results,
        "alerts": [],
    }

    if drifted_features:
        report["alerts"].append(
            f"Data drift detected in {len(drifted_features)} features: {drifted_features}"
        )

    if not log_df.empty and len(log_df) > 100:
        # Rough performance proxy: flag if one class dominates abnormally
        if "segment" in log_df.columns:
            dominant_pct = log_df["segment"].value_counts(normalize=True).max()
            if dominant_pct > 0.90:
                report["alerts"].append(
                    f"WARNING: {dominant_pct:.0%} of predictions are the same class. "
                    "Model may be stuck or dataset is heavily skewed."
                )

    return report


# ─────────────────────────────────────────────────────────────
# Simulate new data for demonstration
# ─────────────────────────────────────────────────────────────

def simulate_new_data(n: int = 500, shift: float = 0.0) -> pd.DataFrame:
    """Generate simulated new production data (optionally shifted)."""
    rng = np.random.default_rng(seed=SIMULATION_SEED)
    return pd.DataFrame({
        "age": rng.integers(22, 70, n),
        "income": np.clip(rng.normal(55_000 + shift * 5000, 20_000, n), 15_000, 200_000),
        "premium_amount": np.clip(rng.normal(1_500, 800, n), 200, 8_000),
        "policy_tenure_months": rng.integers(1, 121, n),
        "num_missed_payments_12m": rng.integers(0, 7, n),
        "avg_payment_delay_days": np.clip(rng.exponential(10 + shift * 5, n), 0, 60),
        "claims_frequency": rng.integers(0, 6, n),
        "credit_score": np.clip(rng.normal(650 - shift * 30, 80, n), 300, 850),
        "payment_consistency_score": np.clip(rng.normal(0.75, 0.15, n), 0, 1),
        "account_age_months": rng.integers(1, 241, n),
    })


# ─────────────────────────────────────────────────────────────
# Main
# ─────────────────────────────────────────────────────────────

def main():
    print("=" * 60)
    print("Step 13: Monitoring & Drift Detection")
    print("=" * 60)

    REPORTS_DIR.mkdir(parents=True, exist_ok=True)

    train_csv = DATA_DIR / "customer_data_processed.csv"
    if not train_csv.exists():
        print("Processed data not found. Run 02_data_preprocessing.py first.")
        return

    train_df = pd.read_csv(train_csv)
    print(f"Training data shape: {train_df.shape}")

    # Simulate new production data (with slight drift)
    print("\nSimulating new production data (with slight distribution shift)…")
    new_df = simulate_new_data(n=SIMULATION_N_SAMPLES, shift=SIMULATION_SHIFT)

    # Detect drift
    print("\nRunning drift detection…")
    drift_results = detect_drift(train_df, new_df)
    drifted = [f for f, v in drift_results.items() if v["drift_detected"]]
    print(f"  Features checked: {len(drift_results)}")
    print(f"  Features with drift: {len(drifted)}")
    if drifted:
        print(f"  Drifted features: {drifted}")

    # Load prediction log (if any)
    log_df = load_prediction_log()
    print(f"\nPrediction log entries: {len(log_df)}")

    monitor_prediction_distribution(log_df)

    # Generate report
    report = generate_monitoring_report(drift_results, log_df)

    report_path = REPORTS_DIR / "monitoring_report.json"
    with open(report_path, "w") as f:
        json.dump(report, f, indent=2)
    print(f"\nMonitoring report saved to: {report_path}")

    if report["alerts"]:
        print("\n⚠  ALERTS:")
        for alert in report["alerts"]:
            print(f"   • {alert}")
    else:
        print("\n✓ No alerts – model appears healthy.")


if __name__ == "__main__":
    main()
