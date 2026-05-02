# REST API Guide

Base URL: `http://localhost:5000`

---

## Endpoints

### `GET /health`

Health check.

**Response 200**
```json
{"status": "ok", "model_loaded": true}
```

---

### `POST /predict`

Predict the risk segment for a single customer.

**Request body**
```json
{
  "age": 35,
  "income": 55000,
  "premium_amount": 1200,
  "policy_tenure_months": 24,
  "num_missed_payments_12m": 2,
  "avg_payment_delay_days": 18,
  "claims_frequency": 1,
  "credit_score": 620,
  "payment_consistency_score": 0.65,
  "account_age_months": 36,
  "policy_type": "Auto",
  "location": "Urban"
}
```

**Response 200**
```json
{
  "segment": "Medium Risk",
  "confidence": 0.8124,
  "probabilities": {
    "High Risk": 0.0531,
    "Low Risk": 0.1345,
    "Medium Risk": 0.8124
  }
}
```

**Error 400**
```json
{"error": "Missing required fields: ['income']"}
```

---

### `POST /predict_batch`

Predict segments for multiple customers.

**Request body** – JSON array of customer objects (same schema as `/predict`).

**Response 200**
```json
{
  "predictions": [
    {"segment": "Low Risk", "confidence": 0.92, "probabilities": {...}},
    null
  ],
  "errors": [
    {"index": 1, "error": "Missing required fields: ['credit_score']"}
  ],
  "total": 2
}
```

---

### `GET /model_info`

Returns model metadata.

**Response 200**
```json
{
  "model_name": "random_forest",
  "model_type": "sklearn",
  "training_date": "2024-01-15T10:30:00",
  "version": "1.0.0",
  "performance": {"accuracy": 0.9241, "f1_weighted": 0.9238},
  "classes": ["High Risk", "Low Risk", "Medium Risk"],
  "task": "multi-class classification"
}
```

---

### `GET /feature_importance`

Returns top 10 most important features.

**Response 200**
```json
{
  "num__num_missed_payments_12m": 0.182341,
  "num__avg_payment_delay_days": 0.163892,
  "num__payment_risk_score": 0.141205,
  "num__credit_score": 0.128467,
  "num__payment_consistency_score": 0.097831
}
```

---

### `GET /model_performance`

Returns evaluation metrics for all trained models.

**Response 200**
```json
{
  "Logistic Regression": {
    "accuracy": 0.8812,
    "precision": 0.8803,
    "recall": 0.8812,
    "f1_score": 0.8806,
    "roc_auc": 0.9541
  },
  "Random Forest": {
    "accuracy": 0.9241,
    "precision": 0.9243,
    "recall": 0.9241,
    "f1_score": 0.9238,
    "roc_auc": 0.9821
  }
}
```

---

## Input Field Reference

| Field | Type | Range/Values |
|-------|------|--------------|
| `age` | int | 22–70 |
| `income` | float | 15000–200000 |
| `premium_amount` | float | 200–8000 |
| `policy_tenure_months` | int | 1–120 |
| `num_missed_payments_12m` | int | 0–6 |
| `avg_payment_delay_days` | float | 0–60 |
| `claims_frequency` | float | 0–10 |
| `credit_score` | float | 300–850 |
| `payment_consistency_score` | float | 0.0–1.0 |
| `account_age_months` | int | 1–240 |
| `policy_type` | string | "Auto", "Life", "Health", "Home" |
| `location` | string | "Urban", "Suburban", "Rural" |

## Error Codes

| Code | Meaning |
|------|---------|
| 400  | Bad request – validation error |
| 500  | Internal server error |
| 503  | Model not loaded |
