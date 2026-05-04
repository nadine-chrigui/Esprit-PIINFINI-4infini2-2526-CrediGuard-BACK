"""
Step 1 & 2: Problem Definition & Data Generation
Generates 5000 synthetic customer records with realistic payment behavior patterns.

Segments:
  - Low Risk:    No missed payments AND avg_payment_delay_days <= 15
  - Medium Risk: missed_payments >= 1 OR avg_payment_delay_days > 15
  - High Risk:   missed_payments >= 3 OR avg_payment_delay_days > 30

Features:
  age, income, premium_amount, policy_tenure_months,
  num_missed_payments_12m, avg_payment_delay_days,
  claims_frequency, credit_score, payment_consistency_score,
  account_age_months, location, policy_type
"""

import os
import numpy as np
import pandas as pd
from pathlib import Path

RANDOM_STATE = 42
N_SAMPLES = 5000

OUTPUT_DIR = Path(__file__).resolve().parent.parent / "data"
OUTPUT_FILE = OUTPUT_DIR / "synthetic_customers.csv"


def assign_segment(row: pd.Series) -> str:
    """Assign risk segment based on payment behavior."""
    if row["num_missed_payments_12m"] >= 3 or row["avg_payment_delay_days"] > 30:
        return "High Risk"
    if row["num_missed_payments_12m"] >= 1 or row["avg_payment_delay_days"] > 15:
        return "Medium Risk"
    return "Low Risk"


def generate_customer_data(n_samples: int = N_SAMPLES, random_state: int = RANDOM_STATE) -> pd.DataFrame:
    """Generate synthetic customer dataset with realistic distributions."""
    rng = np.random.default_rng(random_state)

    # Demographics
    age = rng.integers(22, 70, n_samples)
    income = np.clip(
        rng.normal(55_000, 20_000, n_samples).astype(int),
        15_000, 200_000
    )

    # Policy details — premium correlated with income
    premium_amount = np.clip(
        (income * rng.uniform(0.02, 0.06, n_samples)).astype(int),
        200, 8_000
    )
    policy_tenure_months = rng.integers(1, 121, n_samples)
    policy_type = rng.choice(
        ["Auto", "Life", "Health", "Home"],
        n_samples,
        p=[0.30, 0.30, 0.25, 0.15]
    )

    # Location
    location = rng.choice(
        ["Urban", "Suburban", "Rural"],
        n_samples,
        p=[0.50, 0.35, 0.15]
    )

    # Credit score — correlated with income, realistic range (550–800 mean)
    credit_score = np.clip(
        (500 + (income / 200_000) * 300 + rng.normal(0, 60, n_samples)).astype(int),
        300, 850
    )

    # Payment behavior — correlated with credit score
    # Higher credit score → fewer missed payments and shorter delays
    # credit_factor: 0 = excellent (high credit), 1 = poor (low credit)
    credit_factor = 1 - (credit_score - 300) / 550  # 0 (best) → 1 (worst)

    # Missed payments: 0–6  (most people pay on time → low probability)
    # Scale so that typical (credit_factor ~0.3) → ~10–20% chance of any missed payment
    missed_payment_probs = np.clip(credit_factor * 0.35, 0.01, 0.35)
    num_missed_payments_12m = np.array(
        [rng.binomial(6, p) for p in missed_payment_probs]
    )

    # Payment delay in days: 0–60
    # Typical customer (credit_factor ~0.3): mean ~5 days delay
    avg_payment_delay_days = np.clip(
        (credit_factor * 20 + rng.exponential(3, n_samples)).astype(int),
        0, 60
    )

    # Payment consistency score: 0–1 (higher is better)
    payment_consistency_score = np.clip(
        1.0 - credit_factor * 0.7 + rng.normal(0, 0.05, n_samples),
        0.0, 1.0
    ).round(3)

    # Claims frequency (per year)
    claims_frequency = np.clip(
        rng.poisson(credit_factor * 2.5, n_samples),
        0, 10
    )

    # Account age in months
    account_age_months = np.clip(
        rng.integers(1, 241, n_samples),
        1, 240
    )

    df = pd.DataFrame({
        "customer_id": [f"CUST_{i:05d}" for i in range(1, n_samples + 1)],
        "age": age,
        "income": income,
        "premium_amount": premium_amount,
        "policy_tenure_months": policy_tenure_months,
        "policy_type": policy_type,
        "location": location,
        "credit_score": credit_score,
        "num_missed_payments_12m": num_missed_payments_12m,
        "avg_payment_delay_days": avg_payment_delay_days,
        "payment_consistency_score": payment_consistency_score,
        "claims_frequency": claims_frequency,
        "account_age_months": account_age_months,
    })

    # Assign risk segment
    df["segment"] = df.apply(assign_segment, axis=1)

    return df


def main():
    print("=" * 60)
    print("Step 1 & 2: Data Generation")
    print("=" * 60)

    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

    print(f"Generating {N_SAMPLES} synthetic customer records…")
    df = generate_customer_data()

    print("\nClass distribution:")
    print(df["segment"].value_counts())

    print("\nBasic statistics:")
    print(df.describe())

    df.to_csv(OUTPUT_FILE, index=False)
    print(f"\nData saved to: {OUTPUT_FILE}")
    print(f"Shape: {df.shape}")


if __name__ == "__main__":
    main()
