from __future__ import annotations

from pathlib import Path
from typing import Any, Dict, Tuple
import json
import math

import joblib
import numpy as np
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import GradientBoostingClassifier, RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import accuracy_score, confusion_matrix, f1_score, precision_score, recall_score, roc_auc_score
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder, StandardScaler

FEATURES = ["satisfaction_level", "last_evaluation", "number_project", "average_monthly_hours", "time_spend_company", "work_accident", "promotion_last_5years", "department", "salary", "training_hours", "absenteeism_days", "remote_work_days"]
NUMERIC_FEATURES = [f for f in FEATURES if f not in ("department", "salary")]
CATEGORICAL_FEATURES = ["department", "salary"]
MODEL_VERSION = "hiresense-attrition-v1"


def generate_synthetic_data(records: int = 2000, seed: int = 42) -> pd.DataFrame:
    if records < 1000:
        raise ValueError("records must be at least 1000")
    rng = np.random.default_rng(seed)
    departments = np.array(["sales", "technical", "support", "IT", "product", "marketing", "hr", "finance", "RandD", "accounting", "management"])
    salaries = np.array(["low", "medium", "high"])
    satisfaction = np.clip(rng.beta(5, 2, records), 0.05, 0.99)
    evaluation = np.clip(rng.normal(0.72, 0.14, records), 0.35, 1.0)
    projects = rng.integers(2, 8, records)
    hours = np.clip(rng.normal(200, 45, records), 90, 320)
    tenure = rng.integers(2, 11, records)
    accident = rng.binomial(1, 0.15, records)
    promotion = rng.binomial(1, 0.10, records)
    training = np.clip(rng.normal(65, 25, records), 5, 200)
    absenteeism = np.clip(rng.normal(8, 7, records), 0, 60)
    remote = rng.integers(0, 6, records)
    department = rng.choice(departments, records)
    salary = rng.choice(salaries, records, p=[0.48, 0.42, 0.10])
    logit = (-1.75 - 2.8 * (satisfaction - 0.55) + 0.8 * (evaluation - 0.7) + 0.35 * (projects - 4) + 0.012 * (hours - 200) + 0.22 * (tenure - 4) - 0.45 * promotion - 0.15 * accident + 0.018 * (absenteeism - 8) - 0.25 * (salary == "high") + 0.15 * (salary == "low") + 0.05 * (department == "support"))
    probability = 1 / (1 + np.exp(-logit))
    attrition = rng.binomial(1, probability)
    return pd.DataFrame({"satisfaction_level": satisfaction.round(4), "last_evaluation": evaluation.round(4), "number_project": projects, "average_monthly_hours": hours.round(2), "time_spend_company": tenure, "work_accident": accident, "promotion_last_5years": promotion, "department": department, "salary": salary, "training_hours": training.round(2), "absenteeism_days": absenteeism.round(2), "remote_work_days": remote, "attrition": attrition})


def _preprocessor():
    return ColumnTransformer([("num", StandardScaler(), NUMERIC_FEATURES), ("cat", OneHotEncoder(handle_unknown="ignore"), CATEGORICAL_FEATURES)])


def _metrics(model, x_test, y_test) -> Dict[str, Any]:
    predicted = model.predict(x_test)
    probabilities = model.predict_proba(x_test)[:, 1]
    tn, fp, fn, tp = confusion_matrix(y_test, predicted, labels=[0, 1]).ravel()
    return {"accuracy": round(float(accuracy_score(y_test, predicted)), 4), "precision": round(float(precision_score(y_test, predicted, zero_division=0)), 4), "recall": round(float(recall_score(y_test, predicted, zero_division=0)), 4), "f1": round(float(f1_score(y_test, predicted, zero_division=0)), 4), "roc_auc": round(float(roc_auc_score(y_test, probabilities)), 4), "confusion_matrix": [[int(tn), int(fp)], [int(fn), int(tp)]]}


def train_and_save(records: int = 2000, output_csv: str | Path = "data/attrition_synthetic.csv", model_path: str | Path = "models/attrition_model.joblib", seed: int = 42) -> Dict[str, Any]:
    df = generate_synthetic_data(records, seed)
    output_csv = Path(output_csv); model_path = Path(model_path)
    output_csv.parent.mkdir(parents=True, exist_ok=True); model_path.parent.mkdir(parents=True, exist_ok=True)
    df.to_csv(output_csv, index=False)
    x = df[FEATURES]; y = df["attrition"]
    x_train, x_test, y_train, y_test = train_test_split(x, y, test_size=0.25, random_state=seed, stratify=y)
    models = {
        "RandomForest": RandomForestClassifier(n_estimators=220, random_state=seed, class_weight="balanced", min_samples_leaf=2, n_jobs=-1),
        "LogisticRegression": LogisticRegression(max_iter=1000, class_weight="balanced", random_state=seed),
        "GradientBoosting": GradientBoostingClassifier(n_estimators=160, learning_rate=0.05, max_depth=3, random_state=seed),
    }
    trained, metrics = {}, {}
    for name, estimator in models.items():
        pipeline = Pipeline([("preprocessor", _preprocessor()), ("model", estimator)])
        if name == "GradientBoosting":
            counts = np.bincount(y_train.to_numpy()); sample_weight = np.array([len(y_train) / (2 * counts[int(label)]) for label in y_train])
            pipeline.fit(x_train, y_train, model__sample_weight=sample_weight)
        else:
            pipeline.fit(x_train, y_train)
        trained[name] = pipeline
        metrics[name] = _metrics(pipeline, x_test, y_test)
    best = max(metrics, key=lambda name: (metrics[name]["roc_auc"], metrics[name]["f1"], metrics[name]["recall"]))
    joblib.dump({"model": trained[best], "model_name": best, "version": MODEL_VERSION, "features": FEATURES, "metrics": metrics}, model_path)
    return {"records": len(df), "best_model": best, "metrics": metrics, "model_path": str(model_path)}


def _default_model_path() -> Path:
    return Path(__file__).resolve().parents[1] / "models" / "attrition_model.joblib"


def load_bundle(path: str | Path | None = None):
    model_path = Path(path) if path else _default_model_path()
    if not model_path.exists():
        train_and_save(output_csv=model_path.parent.parent / "data" / "attrition_synthetic.csv", model_path=model_path)
    return joblib.load(model_path)


def predict_attrition(features: Dict[str, Any], path: str | Path | None = None) -> Dict[str, Any]:
    bundle = load_bundle(path)
    frame = pd.DataFrame([{key: features[key] for key in FEATURES}])
    probability = float(bundle["model"].predict_proba(frame)[0, 1])
    if probability < 0.35: risk = "Low"
    elif probability <= 0.65: risk = "Medium"
    else: risk = "High"
    factors = []
    if features["satisfaction_level"] < 0.55: factors.append("Low reported satisfaction")
    if features["average_monthly_hours"] > 240: factors.append("High monthly workload")
    if features["number_project"] >= 6: factors.append("High project load")
    if features["time_spend_company"] >= 7: factors.append("Long tenure without a recent mobility signal")
    if features["promotion_last_5years"] == 0: factors.append("No promotion in the last five years")
    if features["absenteeism_days"] >= 15: factors.append("Elevated absence pattern")
    if not factors: factors.append("No dominant threshold-based risk driver detected")
    actions = []
    if features["satisfaction_level"] < 0.55: actions.append("Schedule a confidential stay interview and address role or workload concerns.")
    if features["average_monthly_hours"] > 240: actions.append("Review capacity, staffing, and sustainable working hours.")
    if features["promotion_last_5years"] == 0: actions.append("Discuss growth goals, skills development, and equitable internal opportunities.")
    if features["number_project"] >= 6: actions.append("Rebalance project assignments and clarify priorities.")
    if not actions: actions.append("Maintain regular check-ins and offer development opportunities; do not treat this score as a decision by itself.")
    return {"probability": round(probability, 4), "risk_level": risk, "top_factors": factors[:5], "suggested_actions": actions[:5], "model_version": bundle.get("version", MODEL_VERSION)}
