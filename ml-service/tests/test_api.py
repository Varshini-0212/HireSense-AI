from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json()["status"] == "ok"


def test_parse_contract():
    response = client.post("/nlp/parse", json={"text": "Jane Doe\njane@example.com\n5 years experience\nSkills: Python, SQL\nEducation\nBSc Computer Science"})
    assert response.status_code == 200
    body = response.json()
    assert {"extracted_name", "email", "phone", "skills", "education", "certifications", "projects", "years_experience"} <= body.keys()
    assert "python" in body["skills"]


def test_match_contract():
    response = client.post("/match", json={"resume_text": "Python SQL engineer with 5 years experience and a bachelor degree", "job_text": "Python SQL engineer, 3 years experience, bachelor degree", "required_skills": ["Python", "SQL"], "candidate_experience_years": 5, "required_experience_years": 3, "candidate_education": "Bachelor", "required_education": "Bachelor"})
    assert response.status_code == 200
    body = response.json()
    assert 0 <= body["overall_score"] <= 100
    assert set(body["component_scores"]) == {"semantic", "skills", "experience", "education"}


def test_attrition_contract():
    response = client.post("/attrition/predict", json={"satisfaction_level": 0.3, "last_evaluation": 0.8, "number_project": 6, "average_monthly_hours": 260, "time_spend_company": 6, "work_accident": 0, "promotion_last_5years": 0, "department": "sales", "salary": "low", "training_hours": 20, "absenteeism_days": 20, "remote_work_days": 0})
    assert response.status_code == 200
    body = response.json()
    assert body["risk_level"] in {"Low", "Medium", "High"}
    assert 0 <= body["probability"] <= 1
    assert body["model_version"]
