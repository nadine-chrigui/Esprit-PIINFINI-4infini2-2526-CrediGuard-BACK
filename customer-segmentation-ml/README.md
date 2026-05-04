# Customer Segmentation ML

A production-ready, end-to-end machine learning pipeline for customer risk segmentation based on payment behavior. Identifies **Low Risk**, **Medium Risk**, and **High Risk** customers to reduce unpaid premiums and improve decision-making.

---

## Project Overview

| Step | Description |
|------|-------------|
| 1–2 | Problem definition & synthetic data generation (5 000 customers) |
| 3   | Data preprocessing, feature engineering, outlier treatment |
| 4   | Exploratory data analysis with visualizations |
| 5   | Label construction (rule-based segmentation) |
| 6   | Stratified 70/30 train-test split |
| 7–8 | Train 4 models with GridSearchCV hyperparameter tuning |
| 9   | Comprehensive evaluation (Accuracy, F1, AUC-ROC, confusion matrices) |
| 10  | SHAP explainability & feature importance |
| 11  | Best model selection & serialization |
| 12  | Flask REST API for real-time & batch predictions |
| 13  | Monitoring, drift detection & automated retraining |

---

## Project Structure

```
customer-segmentation-ml/
├── data/                          # Generated datasets
│   ├── synthetic_customers.csv
│   ├── customer_data_processed.csv
│   └── data_splits.pkl
├── models/                        # Trained model artifacts
│   ├── best_model.pkl
│   ├── preprocessor.pkl
│   └── model_card.json
├── reports/                       # Visualizations & metrics
├── notebooks/
│   └── complete_pipeline.ipynb    # Interactive walkthrough
├── src/
│   ├── 01_data_generation.py
│   ├── 02_data_preprocessing.py
│   ├── 03_eda.py
│   ├── 04_model_training.py
│   ├── 05_model_evaluation.py
│   ├── 06_explainability.py
│   ├── 07_best_model.py
│   ├── app.py                     # Flask REST API
│   ├── monitoring.py
│   └── retraining.py
├── Dockerfile
├── docker-compose.yml
├── requirements.txt
├── API_GUIDE.md
├── DOCUMENTATION.md
└── MONITORING_GUIDE.md
```

---

## Quick Start

### 1. Install dependencies

```bash
pip install -r requirements.txt
```

### 2. Run the full pipeline (in order)

```bash
python src/01_data_generation.py       # Generate 5 000 synthetic customers
python src/02_data_preprocessing.py    # Clean & engineer features
python src/03_eda.py                   # Exploratory analysis
python src/04_model_training.py        # Train & tune 4 models
python src/05_model_evaluation.py      # Evaluate models
python src/06_explainability.py        # SHAP analysis
python src/07_best_model.py            # Select & save best model
```

### 3. Start the API

```bash
python src/app.py
```

### 4. Make a prediction

```bash
curl -X POST http://localhost:5000/predict \
  -H "Content-Type: application/json" \
  -d '{
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
  }'
```

### 5. Docker deployment

```bash
docker-compose up --build
```

---

## Models

| Model | Algorithm | Notes |
|-------|-----------|-------|
| Logistic Regression | Linear | Baseline, interpretable |
| Random Forest | Ensemble | Feature importance |
| XGBoost | Gradient Boosting | High accuracy |
| Neural Network | Deep Learning | Keras/TensorFlow |

All models use **5-fold stratified cross-validation** and **GridSearchCV** for hyperparameter tuning.

---

## Segmentation Rules

| Segment | Condition |
|---------|-----------|
| **High Risk** | `missed_payments ≥ 3` OR `avg_delay > 30 days` |
| **Medium Risk** | `missed_payments ≥ 1` OR `avg_delay > 15 days` |
| **Low Risk** | All others |

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/predict` | Single customer prediction |
| `POST` | `/predict_batch` | Batch predictions |
| `GET`  | `/model_info` | Model metadata |
| `GET`  | `/feature_importance` | Top 10 features |
| `GET`  | `/model_performance` | Evaluation metrics |
| `GET`  | `/health` | Health check |

See [API_GUIDE.md](API_GUIDE.md) for full documentation.

---

## Further Documentation

- [DOCUMENTATION.md](DOCUMENTATION.md) – Architecture & technical details
- [API_GUIDE.md](API_GUIDE.md) – REST API reference
- [MONITORING_GUIDE.md](MONITORING_GUIDE.md) – Monitoring & retraining

---

## License

MIT License
