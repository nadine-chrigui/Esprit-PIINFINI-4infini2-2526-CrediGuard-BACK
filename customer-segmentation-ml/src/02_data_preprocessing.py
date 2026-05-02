"""
Step 3: Data Preparation & Preprocessing
- Handle missing values
- Detect and treat outliers (IQR method)
- Feature engineering
- Normalize numerical features (StandardScaler)
- Encode categorical variables (OneHotEncoder)
- Save preprocessed data and preprocessor objects
"""

import pickle
from pathlib import Path

import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import LabelEncoder, OneHotEncoder, StandardScaler

DATA_DIR = Path(__file__).resolve().parent.parent / "data"
INPUT_FILE = DATA_DIR / "synthetic_customers.csv"
OUTPUT_FILE = DATA_DIR / "customer_data_processed.csv"
SPLITS_FILE = DATA_DIR / "data_splits.pkl"
PREPROCESSOR_FILE = Path(__file__).resolve().parent.parent / "models" / "preprocessor.pkl"

NUMERICAL_FEATURES = [
    "age",
    "income",
    "premium_amount",
    "policy_tenure_months",
    "num_missed_payments_12m",
    "avg_payment_delay_days",
    "claims_frequency",
    "credit_score",
    "payment_consistency_score",
    "account_age_months",
    # Derived features added below
    "premium_to_income_ratio",
    "payment_risk_score",
    "tenure_to_age_ratio",
]

CATEGORICAL_FEATURES = ["policy_type", "location"]
TARGET = "segment"

RANDOM_STATE = 42
TEST_SIZE = 0.30


# ─────────────────────────────────────────────────────────────
# Outlier treatment
# ─────────────────────────────────────────────────────────────

def treat_outliers_iqr(df: pd.DataFrame, columns: list) -> pd.DataFrame:
    """Cap outliers at IQR boundaries (1.5×IQR)."""
    df = df.copy()
    for col in columns:
        q1 = df[col].quantile(0.25)
        q3 = df[col].quantile(0.75)
        iqr = q3 - q1
        lower = q1 - 1.5 * iqr
        upper = q3 + 1.5 * iqr
        before = ((df[col] < lower) | (df[col] > upper)).sum()
        df[col] = df[col].clip(lower=lower, upper=upper)
        if before > 0:
            print(f"  {col}: capped {before} outliers [{lower:.2f}, {upper:.2f}]")
    return df


# ─────────────────────────────────────────────────────────────
# Feature engineering
# ─────────────────────────────────────────────────────────────

def engineer_features(df: pd.DataFrame) -> pd.DataFrame:
    """Create derived features."""
    df = df.copy()
    df["premium_to_income_ratio"] = (df["premium_amount"] / df["income"].replace(0, np.nan)).round(4)
    df["payment_risk_score"] = (
        df["num_missed_payments_12m"] * 10
        + df["avg_payment_delay_days"] * 0.5
        + (1 - df["payment_consistency_score"]) * 20
    ).round(2)
    df["tenure_to_age_ratio"] = (df["policy_tenure_months"] / (df["age"] * 12)).round(4)
    return df


# ─────────────────────────────────────────────────────────────
# Main
# ─────────────────────────────────────────────────────────────

def main():
    print("=" * 60)
    print("Step 3: Data Preprocessing")
    print("=" * 60)

    (DATA_DIR / "..").resolve()  # ensure structure
    PREPROCESSOR_FILE.parent.mkdir(parents=True, exist_ok=True)

    # Load
    print(f"\nLoading data from {INPUT_FILE}…")
    df = pd.read_csv(INPUT_FILE)
    print(f"Original shape: {df.shape}")

    # Drop non-feature columns
    df = df.drop(columns=["customer_id"], errors="ignore")

    # Missing values
    print(f"\nMissing values before fill: {df.isnull().sum().sum()}")
    df[CATEGORICAL_FEATURES] = df[CATEGORICAL_FEATURES].fillna("Unknown")
    base_num_cols = [
        "age", "income", "premium_amount", "policy_tenure_months",
        "num_missed_payments_12m", "avg_payment_delay_days",
        "claims_frequency", "credit_score", "payment_consistency_score",
        "account_age_months",
    ]
    df[base_num_cols] = df[base_num_cols].fillna(df[base_num_cols].median())
    print(f"Missing values after fill:  {df.isnull().sum().sum()}")

    # Feature engineering
    print("\nEngineering features…")
    df = engineer_features(df)

    # Outlier treatment (numerical cols only)
    iqr_cols = [c for c in NUMERICAL_FEATURES if c in df.columns]
    print("\nTreating outliers (IQR)…")
    df = treat_outliers_iqr(df, iqr_cols)

    # Save processed CSV
    df.to_csv(OUTPUT_FILE, index=False)
    print(f"\nProcessed data saved to: {OUTPUT_FILE}")

    # ── Build preprocessor ──────────────────────────────────
    feature_cols = NUMERICAL_FEATURES + CATEGORICAL_FEATURES

    preprocessor = ColumnTransformer(
        transformers=[
            ("num", StandardScaler(), NUMERICAL_FEATURES),
            ("cat", OneHotEncoder(handle_unknown="ignore", sparse_output=False), CATEGORICAL_FEATURES),
        ]
    )

    # ── Encode target ───────────────────────────────────────
    label_encoder = LabelEncoder()
    y = label_encoder.fit_transform(df[TARGET])

    X = df[feature_cols]

    # ── Train/test split ────────────────────────────────────
    X_train, X_test, y_train, y_test = train_test_split(
        X, y,
        test_size=TEST_SIZE,
        stratify=y,
        random_state=RANDOM_STATE,
    )

    # Fit preprocessor on train only
    X_train_processed = preprocessor.fit_transform(X_train)
    X_test_processed = preprocessor.transform(X_test)

    print(f"\nTrain shape: {X_train_processed.shape}")
    print(f"Test shape:  {X_test_processed.shape}")
    print(f"\nClass distribution (train): {np.bincount(y_train)}")
    print(f"Class names: {label_encoder.classes_}")

    # ── Save splits & preprocessor ──────────────────────────
    splits = {
        "X_train": X_train_processed,
        "X_test": X_test_processed,
        "y_train": y_train,
        "y_test": y_test,
        "feature_names": feature_cols,
        "class_names": list(label_encoder.classes_),
        "label_encoder": label_encoder,
    }

    with open(SPLITS_FILE, "wb") as f:
        pickle.dump(splits, f)
    print(f"Data splits saved to:  {SPLITS_FILE}")

    with open(PREPROCESSOR_FILE, "wb") as f:
        pickle.dump({"preprocessor": preprocessor, "label_encoder": label_encoder}, f)
    print(f"Preprocessor saved to: {PREPROCESSOR_FILE}")


if __name__ == "__main__":
    main()
