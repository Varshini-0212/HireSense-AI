from __future__ import annotations

from typing import Any, Dict, List, Optional
from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator


class NLPParseRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")
    text: str = Field(..., min_length=1, max_length=100_000)

    @field_validator("text")
    @classmethod
    def non_blank(cls, value: str) -> str:
        if not value.strip():
            raise ValueError("text must not be blank")
        return value.strip()


class NLPParseResponse(BaseModel):
    extracted_name: Optional[str] = None
    email: Optional[str] = None
    phone: Optional[str] = None
    skills: List[str]
    education: List[str]
    certifications: List[str]
    projects: List[str]
    years_experience: Optional[float] = None


class MatchWeights(BaseModel):
    model_config = ConfigDict(extra="ignore")
    semantic: float = Field(0.40, ge=0, le=1)
    skills: float = Field(0.35, ge=0, le=1)
    experience: float = Field(0.15, ge=0, le=1)
    education: float = Field(0.10, ge=0, le=1)

    @model_validator(mode="after")
    def positive_total(self) -> "MatchWeights":
        if self.semantic + self.skills + self.experience + self.education <= 0:
            raise ValueError("at least one matching weight must be greater than zero")
        return self

    def normalized(self) -> Dict[str, float]:
        values = {"semantic": self.semantic, "skills": self.skills, "experience": self.experience, "education": self.education}
        total = sum(values.values())
        return {key: value / total for key, value in values.items()}


class MatchRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")
    resume_text: str = Field(..., min_length=1, max_length=100_000)
    job_text: str = Field(..., min_length=1, max_length=100_000)
    resume_skills: List[str] = Field(default_factory=list, max_length=200)
    required_skills: List[str] = Field(default_factory=list, max_length=200)
    preferred_skills: List[str] = Field(default_factory=list, max_length=200)
    candidate_experience_years: Optional[float] = Field(None, ge=0, le=80)
    required_experience_years: Optional[float] = Field(None, ge=0, le=80)
    candidate_education: Optional[str] = Field(None, max_length=300)
    required_education: Optional[str] = Field(None, max_length=300)
    weights: MatchWeights = Field(default_factory=MatchWeights)

    @model_validator(mode="before")
    @classmethod
    def common_aliases(cls, values: Any) -> Any:
        if not isinstance(values, dict):
            return values
        data = dict(values)
        aliases = {"resume": "resume_text", "candidate_text": "resume_text", "candidate_resume": "resume_text", "job_description": "job_text", "job": "job_text"}
        for source, target in aliases.items():
            if target not in data and source in data:
                data[target] = data[source]
        if "resume_skills" not in data and "candidate_skills" in data:
            data["resume_skills"] = data["candidate_skills"]
        return data


class MatchResponse(BaseModel):
    overall_score: float
    component_scores: Dict[str, float]
    matched_skills: List[str]
    missing_skills: List[str]
    section_feedback: List[str]
    improvement_tips: List[str]


class AttritionRequest(BaseModel):
    model_config = ConfigDict(extra="ignore")
    satisfaction_level: float = Field(..., ge=0, le=1)
    last_evaluation: float = Field(..., ge=0, le=1)
    number_project: int = Field(..., ge=0, le=50)
    average_monthly_hours: float = Field(..., ge=0, le=1000)
    time_spend_company: float = Field(..., ge=0, le=100)
    work_accident: int = Field(0, ge=0, le=1)
    promotion_last_5years: int = Field(0, ge=0, le=1)
    department: str = Field("sales", min_length=1, max_length=80)
    salary: str = Field("medium", min_length=1, max_length=30)
    training_hours: float = Field(0, ge=0, le=1000)
    absenteeism_days: float = Field(0, ge=0, le=365)
    remote_work_days: float = Field(0, ge=0, le=366)

    @model_validator(mode="before")
    @classmethod
    def unpack_employee(cls, values: Any) -> Any:
        if isinstance(values, dict) and isinstance(values.get("employee"), dict):
            merged = dict(values["employee"])
            merged.update({k: v for k, v in values.items() if k != "employee"})
            return merged
        return values


class AttritionResponse(BaseModel):
    probability: float
    risk_level: str
    top_factors: List[str]
    suggested_actions: List[str]
    model_version: str


class TrainingResponse(BaseModel):
    records: int
    best_model: str
    metrics: Dict[str, Dict[str, Any]]
    model_path: str
