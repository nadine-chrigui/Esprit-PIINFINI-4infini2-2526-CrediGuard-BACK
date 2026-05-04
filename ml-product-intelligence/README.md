# Product Intelligence ML Service

Small Flask service for the e-commerce Product Intelligence V1.

## Run

```bash
cd ml-product-intelligence
python -m venv .venv
.venv\Scripts\activate
pip install -r requirements.txt
python app.py
```

The service runs on:

```text
http://localhost:5001
```

## Health check

```http
GET http://localhost:5001/health
```

## Model info

```http
GET http://localhost:5001/model-info
```

If no trained model exists yet, Flask uses the rule-based fallback.

## Train a real model

Create a real training dataset:

```text
data/product_intelligence_training.csv
```

Use this template as a starting point:

```text
data/product_intelligence_training_template.csv
```

Required columns:

```csv
stock,sales_last_7_days,sales_last_30_days,price,risk_level,performance_score,performance_label,suggested_action
```

Train:

```bash
python train_model.py
```

This creates:

```text
models/product_intelligence_model.joblib
models/product_intelligence_metadata.json
```

Restart Flask after training.

## Predict product intelligence

```http
POST http://localhost:5001/predict/product-intelligence
Content-Type: application/json
```

Example body:

```json
{
  "product_id": 1,
  "stock": 20,
  "sales_last_7_days": 35,
  "sales_last_30_days": 120,
  "price": 50.0
}
```

Example response:

```json
{
  "product_id": 1,
  "risk_level": "HIGH",
  "days_to_stockout": 4,
  "recommended_restock": 130,
  "performance_score": 95,
  "performance_label": "AT_RISK",
  "suggested_action": "RESTOCK",
  "reasons": [
    "Stock may run out in 4 day(s).",
    "35 unit(s) sold in the last 7 days.",
    "120 unit(s) sold in the last 30 days."
  ]
}
```

## Notes

The service first tries to use a trained model. If no model is found, it falls back to explainable business rules without changing the Spring Boot integration contract.
