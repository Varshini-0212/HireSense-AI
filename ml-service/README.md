# HireSense AI ML Service

FastAPI microservice for corpus-free resume parsing, explainable candidate matching, and employee attrition risk estimation. It requires no API keys or downloaded NLP corpora.

## Run locally

```bash
cd ml-service
python3 -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
python scripts/train_attrition.py --records 2000
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

The service is available at `http://localhost:8000`; OpenAPI docs are at `/docs`.

## Endpoints

- `GET /health` returns `{ "status": "ok", "service": "hiresense-ml", "version": "1.0.0" }`.
- `POST /nlp/parse` accepts `{ "text": "..." }` and returns `extracted_name`, `email`, `phone`, `skills`, `education`, `certifications`, `projects`, and `years_experience`.
- `POST /match` accepts `resume_text`, `job_text`, `resume_skills`, `required_skills`, `preferred_skills`, experience/education fields, and optional weights. It returns `overall_score` (0–100), `component_scores` for semantic/skills/experience/education, matched/missing skills, section feedback, and improvement tips. Defaults are 0.40 / 0.35 / 0.15 / 0.10 and are normalized if custom weights do not sum to one.
- `POST /attrition/predict` accepts the approved employee feature fields and returns `probability`, `risk_level`, `top_factors`, `suggested_actions`, and `model_version`.
- `POST /attrition/train` accepts optional `{ "records": 2000, "seed": 42 }`, requiring at least 1,000 records, and compares all three classifiers before serializing the best model.

## Modeling details

Matching uses scikit-learn TF-IDF word n-grams and cosine similarity, plus normalized skill overlap and transparent experience/education scores. The skill catalog contains more than 300 technical and soft skill names with aliases and is bundled in source code.

Attrition data is generated deterministically with NumPy (`seed=42`) and includes 2,000 records by default. Training evaluates **RandomForestClassifier**, **LogisticRegression**, and **GradientBoostingClassifier**. Random Forest and Logistic Regression use `class_weight="balanced"`; Gradient Boosting uses inverse-frequency sample weights. The artifact includes accuracy, precision, recall, F1, ROC-AUC, and a 2×2 confusion matrix for every candidate model. The highest ROC-AUC (then F1 and recall) is serialized to `models/attrition_model.joblib`.

Risk bands are **Low** when probability < 0.35, **Medium** when 0.35–0.65 inclusive, and **High** when > 0.65.

The attrition feature policy uses job-relevant operational fields: satisfaction, evaluation, project load, hours, tenure, work accident, promotion history, department, salary band, training, absences, and remote-work days. It does **not** use candidate name, gender, age, or photo for scoring. Predictions are decision support only, not a basis for automatic employment action; HR should validate data quality, check fairness, and use confidential human review.

## Tests

```bash
pytest -q
```

## Docker

```bash
docker build -t hiresense-ml .
docker run --rm -p 8000:8000 hiresense-ml
```
