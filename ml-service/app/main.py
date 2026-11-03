from __future__ import annotations

from pathlib import Path
from typing import Any, Dict

from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse

from .attrition_model import FEATURES, predict_attrition, train_and_save
from .matching import match_candidate
from .nlp.parser import parse_text
from .schemas import AttritionRequest, AttritionResponse, MatchRequest, MatchResponse, NLPParseRequest, NLPParseResponse, TrainingResponse

app = FastAPI(title="HireSense AI ML Service", version="1.0.0", description="Offline-capable NLP, candidate matching, and attrition prediction service")


@app.get("/health")
def health() -> Dict[str, str]:
    return {"status": "ok", "service": "hiresense-ml", "version": "1.0.0"}


@app.post("/nlp/parse", response_model=NLPParseResponse)
def parse_resume(request: NLPParseRequest):
    return parse_text(request.text)


@app.post("/match", response_model=MatchResponse)
def match(request: MatchRequest):
    weights = request.weights.normalized()
    return match_candidate(
        resume_text=request.resume_text,
        job_text=request.job_text,
        resume_skills=request.resume_skills,
        required_skills=request.required_skills,
        preferred_skills=request.preferred_skills,
        candidate_experience_years=request.candidate_experience_years,
        required_experience_years=request.required_experience_years,
        candidate_education=request.candidate_education,
        required_education=request.required_education,
        weights=weights,
    )


@app.post("/attrition/predict", response_model=AttritionResponse)
def attrition_predict(request: AttritionRequest):
    values = request.model_dump()
    # Explicitly select the approved employee feature policy; any ignored input such as
    # name, gender, age, or photo cannot reach the model.
    selected = {key: values[key] for key in FEATURES}
    return predict_attrition(selected)


@app.post("/attrition/train", response_model=TrainingResponse)
def attrition_train(payload: Dict[str, Any] | None = None):
    payload = payload or {}
    records = int(payload.get("records", 2000))
    if records < 1000 or records > 100_000:
        raise HTTPException(status_code=422, detail="records must be between 1000 and 100000")
    seed = int(payload.get("seed", 42))
    base = Path(__file__).resolve().parents[1]
    result = train_and_save(records=records, seed=seed, output_csv=base / "data" / "attrition_synthetic.csv", model_path=base / "models" / "attrition_model.joblib")
    return result


@app.exception_handler(ValueError)
def value_error_handler(_, exc: ValueError):
    return JSONResponse(status_code=422, content={"detail": str(exc)})
