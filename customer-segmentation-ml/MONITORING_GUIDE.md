# Monitoring Guide

## Overview

The monitoring pipeline (`src/monitoring.py`) tracks data drift and model prediction health. The retraining pipeline (`src/retraining.py`) automatically updates the production model when performance improves.

---

## Running Monitoring

```bash
python src/monitoring.py
```

Output:
- `reports/13_prediction_distribution.png` – Distribution of predicted segments
- `reports/monitoring_report.json` – Full drift & alert report

---

## Drift Detection Methods

### KS Test (Kolmogorov-Smirnov)

Compares the distribution of each feature between training data and new production data.

- **Threshold**: p-value < 0.05 → drift detected
- Lower p-value = stronger evidence of distribution change

### PSI (Population Stability Index)

Measures the shift in feature distributions using binned percentiles.

| PSI Value | Interpretation |
|-----------|---------------|
| < 0.10 | No significant change |
| 0.10–0.20 | Moderate change – monitor |
| > 0.20 | **Significant drift – investigate** |

---

## Prediction Logging

To log a prediction for monitoring:

```python
from monitoring import log_prediction

log_prediction({
    "segment": "Medium Risk",
    "confidence": 0.82,
    "input_age": 35,
    "input_credit_score": 620,
})
```

Logs are stored in `logs/predictions.jsonl` (one JSON object per line).

---

## Retraining Pipeline

```bash
python src/retraining.py
```

The pipeline:
1. Loads new data (simulated or from `data/new_data.csv` if available)
2. Retrains a Random Forest on original + new data
3. Evaluates the new model on the held-out test set
4. Replaces the production model if `new_f1 − current_f1 ≥ 0.005`
5. Logs the retraining event to `logs/retraining_history.json`

### Retraining Triggers

| Condition | Action |
|-----------|--------|
| F1 improves ≥ 0.5% | Update production model |
| Drift detected in ≥ 3 features | Schedule retraining |
| Dominant class > 90% of predictions | Investigate + consider retrain |
| No current production model | Always train |

---

## Monitoring Report Schema

```json
{
  "report_timestamp": "2024-01-15T10:30:00",
  "total_predictions_logged": 1523,
  "drift_summary": {
    "features_checked": 10,
    "features_drifted": 2,
    "drifted_features": ["credit_score", "avg_payment_delay_days"]
  },
  "drift_details": {
    "credit_score": {
      "ks_statistic": 0.1823,
      "ks_pvalue": 0.0012,
      "psi": 0.2341,
      "drift_detected": true
    }
  },
  "alerts": [
    "Data drift detected in 2 features: ['credit_score', 'avg_payment_delay_days']"
  ]
}
```

---

## Recommended Monitoring Schedule

| Frequency | Task |
|-----------|------|
| Daily | Check prediction distribution |
| Weekly | Run drift detection |
| Monthly | Full retraining evaluation |
| On alert | Investigate immediately |
