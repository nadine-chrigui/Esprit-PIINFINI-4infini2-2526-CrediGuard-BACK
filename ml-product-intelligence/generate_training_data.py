from __future__ import annotations

import csv
import random
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent
DATA_PATH = BASE_DIR / "data" / "product_intelligence_training.csv"


def clamp(value: int, minimum: int, maximum: int) -> int:
    return max(minimum, min(maximum, value))


def classify_row(stock: int, sales_7: int, sales_30: int, price: float):
    daily_velocity = sales_7 / 7 if sales_7 > 0 else sales_30 / 30
    days_to_stockout = 999 if daily_velocity <= 0 else max(1, int(stock // daily_velocity))

    if stock <= 0 or (daily_velocity > 0 and days_to_stockout <= 5):
        risk_level = "HIGH"
    elif days_to_stockout <= 14:
        risk_level = "MEDIUM"
    else:
        risk_level = "LOW"

    if risk_level == "HIGH" and sales_7 > 0:
        performance_label = "AT_RISK"
    elif sales_30 >= 50 or sales_7 >= 15:
        performance_label = "BEST_SELLER"
    elif sales_30 <= 3 and stock >= 20:
        performance_label = "SLOW_MOVING"
    else:
        performance_label = "STABLE"

    score = 45
    score += min(30, sales_30)
    score += min(15, sales_7)

    if performance_label == "BEST_SELLER":
        score += 15
    if performance_label == "SLOW_MOVING":
        score -= 20
    if risk_level == "HIGH":
        score -= 15
    if stock <= 0:
        score -= 10
    if price > 0 and sales_30 > 0:
        score += 5

    performance_score = clamp(score, 0, 100)

    if risk_level == "HIGH" or stock <= 0:
        suggested_action = "RESTOCK"
    elif performance_label == "SLOW_MOVING" or sales_30 <= 3:
        suggested_action = "PROMOTE"
    elif risk_level == "MEDIUM":
        suggested_action = "MONITOR"
    else:
        suggested_action = "KEEP"

    return risk_level, performance_score, performance_label, suggested_action


def generate_rows(count: int = 500):
    random.seed(42)
    rows = []

    scenarios = [
        "best_seller_low_stock",
        "slow_moving_high_stock",
        "stable_normal",
        "medium_risk",
        "out_of_stock",
        "expensive_low_volume",
    ]

    for _ in range(count):
        scenario = random.choice(scenarios)

        if scenario == "best_seller_low_stock":
            stock = random.randint(0, 35)
            sales_7 = random.randint(15, 60)
            sales_30 = random.randint(max(sales_7, 50), 180)
            price = round(random.uniform(20, 250), 2)
        elif scenario == "slow_moving_high_stock":
            stock = random.randint(40, 250)
            sales_7 = random.randint(0, 1)
            sales_30 = random.randint(0, 3)
            price = round(random.uniform(10, 180), 2)
        elif scenario == "stable_normal":
            stock = random.randint(30, 180)
            sales_7 = random.randint(3, 14)
            sales_30 = random.randint(12, 49)
            price = round(random.uniform(15, 220), 2)
        elif scenario == "medium_risk":
            stock = random.randint(10, 80)
            sales_7 = random.randint(5, 20)
            sales_30 = random.randint(20, 75)
            price = round(random.uniform(15, 260), 2)
        elif scenario == "out_of_stock":
            stock = 0
            sales_7 = random.randint(0, 25)
            sales_30 = random.randint(sales_7, 90)
            price = round(random.uniform(8, 200), 2)
        else:
            stock = random.randint(10, 120)
            sales_7 = random.randint(0, 8)
            sales_30 = random.randint(sales_7, 25)
            price = round(random.uniform(250, 1200), 2)

        risk_level, performance_score, performance_label, suggested_action = classify_row(
            stock,
            sales_7,
            sales_30,
            price,
        )

        rows.append({
            "stock": stock,
            "sales_last_7_days": sales_7,
            "sales_last_30_days": sales_30,
            "price": price,
            "risk_level": risk_level,
            "performance_score": performance_score,
            "performance_label": performance_label,
            "suggested_action": suggested_action,
        })

    return rows


def main() -> None:
    DATA_PATH.parent.mkdir(parents=True, exist_ok=True)
    rows = generate_rows(500)

    with DATA_PATH.open("w", newline="", encoding="utf-8") as file:
        writer = csv.DictWriter(file, fieldnames=list(rows[0].keys()))
        writer.writeheader()
        writer.writerows(rows)

    print(f"Generated {len(rows)} rows at {DATA_PATH}")


if __name__ == "__main__":
    main()
