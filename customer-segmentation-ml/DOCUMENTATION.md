# Technical Documentation

## Architecture

```
Data Generation → Preprocessing → EDA → Model Training → Evaluation
       ↓               ↓                       ↓               ↓
  5000 records    Cleaned CSV           4 ML Models      Reports/Plots
                  data_splits.pkl       best_model.pkl
                  preprocessor.pkl
                       ↓
                  Flask REST API  ←  Monitoring  ←  Retraining
```

## Data Pipeline

### Input Features

| Feature | Type | Description |
|---------|------|-------------|
| `age` | int | Customer age (22–70) |
| `income` | float | Annual income ($) |
| `premium_amount` | float | Policy premium ($) |
| `policy_tenure_months` | int | Policy age in months |
| `num_missed_payments_12m` | int | Missed payments last 12 months |
| `avg_payment_delay_days` | float | Average delay in days |
| `claims_frequency` | float | Claims per year |
| `credit_score` | float | Credit score (300–850) |
| `payment_consistency_score` | float | 0–1 consistency score |
| `account_age_months` | int | Account age in months |
| `policy_type` | str | Auto, Life, Health, Home |
| `location` | str | Urban, Suburban, Rural |

### Derived Features

| Feature | Formula |
|---------|---------|
| `premium_to_income_ratio` | `premium / income` |
| `payment_risk_score` | `missed×10 + delay×0.5 + (1−consistency)×20` |
| `tenure_to_age_ratio` | `tenure_months / (age × 12)` |

## Preprocessing

1. **Missing value imputation** – median for numerical, "Unknown" for categorical
2. **Outlier treatment** – IQR capping at 1.5×IQR boundaries
3. **Scaling** – StandardScaler for all numerical features
4. **Encoding** – OneHotEncoder for categorical features (policy_type, location)
5. **Label encoding** – LabelEncoder for the target (segment)

## Models

### Logistic Regression
- Solver: lbfgs
- Regularization: C ∈ {0.01, 0.1, 1.0, 10.0}
- Multi-class: softmax

### Random Forest
- n_estimators: 100–200
- max_depth: None, 10, 20
- min_samples_split: 2, 5

### XGBoost
- n_estimators: 100–200
- max_depth: 3–6
- learning_rate: 0.05–0.2
- eval_metric: mlogloss

### Neural Network (Keras)
- Architecture: Input → Dense(128) → Dropout(0.3) → Dense(64) → Dropout(0.2) → Dense(32) → Softmax
- Optimizer: Adam (lr=0.001)
- Loss: sparse_categorical_crossentropy
- Early stopping: patience=5, monitor=val_loss

## Evaluation Metrics

- **Accuracy** – overall correct predictions
- **Precision** – positive predictive value (weighted)
- **Recall** – sensitivity (weighted)
- **F1-Score** – harmonic mean of precision and recall (weighted)
- **AUC-ROC** – area under ROC curve (one-vs-rest, weighted)

## Serialization

| File | Contents |
|------|----------|
| `models/best_model.pkl` | Best sklearn model (joblib) |
| `models/best_model.keras` | Best Keras model (if NN wins) |
| `models/preprocessor.pkl` | ColumnTransformer + LabelEncoder |
| `models/model_card.json` | Metadata, performance, classes |
| `data/data_splits.pkl` | Train/test arrays + class names |

## Monitoring

The monitoring pipeline computes:

- **KS test** – detects distributional shift (p-value threshold: 0.05)
- **PSI** – Population Stability Index (threshold: 0.2)
- **Prediction distribution tracking** – alerts if one class dominates (>90%)

## Retraining Trigger

New model replaces production if:
- `new_f1 − current_f1 ≥ 0.005` (0.5% improvement), OR
- No current production model exists
