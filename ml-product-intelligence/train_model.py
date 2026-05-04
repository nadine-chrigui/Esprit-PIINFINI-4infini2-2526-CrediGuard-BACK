from __future__ import annotations

import argparse
import json
from pathlib import Path

import joblib
import pandas as pd
from sklearn.ensemble import RandomForestClassifier, RandomForestRegressor
from sklearn.metrics import accuracy_score, mean_absolute_error
from sklearn.model_selection import train_test_split
from sklearn.multioutput import MultiOutputClassifier
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler


BASE_DIR = Path(__file__).resolve().parent
DEFAULT_DATASET = BASE_DIR / "data" / "product_intelligence_training.csv"
TEMPLATE_DATASET = BASE_DIR / "data" / "product_intelligence_training_template.csv"
MODEL_DIR = BASE_DIR / "models"
MODEL_PATH = MODEL_DIR / "product_intelligence_model.joblib"
METADATA_PATH = MODEL_DIR / "product_intelligence_metadata.json"

FEATURE_COLUMNS = [
    "stock",
    "sales_last_7_days",
    "sales_last_30_days",
    "price",
]

CLASS_TARGETS = [
    "risk_level",
    "performance_label",
    "suggested_action",
]

REGRESSION_TARGET = "performance_score"


def load_dataset(path: Path) -> pd.DataFrame:
    if not path.exists():
        raise FileNotFoundError(
            f"Dataset not found: {path}\n"
            f"Create it from the template: {TEMPLATE_DATASET}"
        )

    df = pd.read_csv(path)
    required = FEATURE_COLUMNS + CLASS_TARGETS + [REGRESSION_TARGET]
    missing = [column for column in required if column not in df.columns]

    if missing:
        raise ValueError(f"Missing required columns: {', '.join(missing)}")

    df = df[required].dropna()

    if len(df) < 20:
        raise ValueError(
            "A real ML model needs more data. Add at least 20 rows, preferably 100+ rows."
        )

    return df


def train(dataset_path: Path) -> None:
    df = load_dataset(dataset_path)

    x = df[FEATURE_COLUMNS]
    y_class = df[CLASS_TARGETS]
    y_score = df[REGRESSION_TARGET]

    stratify_target = y_class["risk_level"] if y_class["risk_level"].nunique() > 1 else None

    x_train, x_test, y_class_train, y_class_test, y_score_train, y_score_test = train_test_split(
        x,
        y_class,
        y_score,
        test_size=0.2,
        random_state=42,
        stratify=stratify_target,
    )

    classifier = Pipeline(
        steps=[
            ("scaler", StandardScaler()),
            (
                "model",
                MultiOutputClassifier(
                    RandomForestClassifier(
                        n_estimators=220,
                        random_state=42,
                        class_weight="balanced_subsample",
                    )
                ),
            ),
        ]
    )

    regressor = Pipeline(
        steps=[
            ("scaler", StandardScaler()),
            (
                "model",
                RandomForestRegressor(
                    n_estimators=220,
                    random_state=42,
                ),
            ),
        ]
    )

    classifier.fit(x_train, y_class_train)
    regressor.fit(x_train, y_score_train)

    y_class_pred = classifier.predict(x_test)
    y_score_pred = regressor.predict(x_test)

    class_metrics = {}
    for index, target in enumerate(CLASS_TARGETS):
        class_metrics[target + "_accuracy"] = accuracy_score(
            y_class_test[target],
            y_class_pred[:, index],
        )

    score_mae = mean_absolute_error(y_score_test, y_score_pred)

    MODEL_DIR.mkdir(parents=True, exist_ok=True)
    joblib.dump(
        {
            "feature_columns": FEATURE_COLUMNS,
            "classifier": classifier,
            "regressor": regressor,
        },
        MODEL_PATH,
    )

    metadata = {
        "model_type": "RandomForestClassifier + RandomForestRegressor",
        "dataset": str(dataset_path),
        "rows": int(len(df)),
        "feature_columns": FEATURE_COLUMNS,
        "class_targets": CLASS_TARGETS,
        "regression_target": REGRESSION_TARGET,
        "metrics": {
            **class_metrics,
            "performance_score_mae": score_mae,
        },
    }

    METADATA_PATH.write_text(json.dumps(metadata, indent=2), encoding="utf-8")

    print("Model trained successfully.")
    print(f"Model saved to: {MODEL_PATH}")
    print(f"Metadata saved to: {METADATA_PATH}")
    print(json.dumps(metadata["metrics"], indent=2))


def main() -> None:
    parser = argparse.ArgumentParser(description="Train Product Intelligence ML model.")
    parser.add_argument(
        "--dataset",
        default=str(DEFAULT_DATASET),
        help="CSV dataset path. Default: data/product_intelligence_training.csv",
    )

    args = parser.parse_args()
    train(Path(args.dataset))


if __name__ == "__main__":
    main()
