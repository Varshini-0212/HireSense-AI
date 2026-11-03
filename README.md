# HireSense AI

**HireSense AI – Resume Screening, Job Matching & Attrition Prediction** is a phased full-stack recruitment and workforce-analytics application. The repository now includes the MySQL/Spring persistence layer, JWT-protected APIs, PDF/DOCX resume parsing, a Python NLP/matching/attrition service, a React dashboard, PDF match reports, tests, and Docker Compose configuration.

> **Decision-support notice:** Candidate match scores and employee attrition probabilities are explainable signals only. They are not hiring, promotion, termination, or other employment decisions. Human review is required. The included attrition model is trained on synthetic records and is not validated for real workforce use.

## Project structure

```text
hiresense-ai/
├── .env.example
├── docker-compose.yml
├── README.md
├── backend/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/hiresense/
│       │   ├── config/       # Security, HTTP client, API audit interceptor
│       │   ├── controller/   # Auth, profile, resume, job, match, HR, admin, reports
│       │   ├── domain/       # JPA entities and workflow enums
│       │   ├── dto/          # Validated request and safe response records
│       │   ├── exception/    # API errors and global exception handling
│       │   ├── repository/  # Spring Data repositories
│       │   ├── security/    # JWT filter, token service, user details
│       │   ├── service/     # Business logic, parsing, reporting, ML client
│       │   └── resources/   # MySQL schema.sql, data.sql, application.yml
│       └── test/             # H2 persistence, API security, and job-skill integration tests
├── ml-service/
│   ├── Dockerfile
│   ├── app/                  # FastAPI routes, NLP, TF-IDF matching, models
│   ├── scripts/              # Synthetic data generator and training script
│   ├── tests/                # pytest API/model tests
│   └── requirements.txt
├── frontend/
│   ├── Dockerfile
│   ├── src/App.jsx           # Responsive role-aware application pages
│   ├── src/context/          # Authentication state
│   └── src/lib/api.js        # Spring API adapter and error/page helpers
└── docs/
    ├── academic-report.md    # UML diagrams, test matrix, sample data and outputs
    ├── phase-1.md            # Persistence notes
    └── sdg-feature-map.md    # SDG 4, 5, 8, 9, 10 alignment and guardrails
```

## Implemented modules

1. **Persistence:** MySQL 8 schema, foreign keys, indexes, JPA entities, repositories, and idempotent local seed data (1 admin, 2 HR users, 5 candidates, 10 jobs, and 50 synthetic employees).
2. **Authentication and access:** BCrypt password hashing, JWT access tokens, role-based API authorization, candidate public signup, admin-managed accounts, profile read/update, and user activation controls.
3. **Resume screening:** PDFBox PDF and Apache POI DOCX text extraction, a curated 300+ canonical-skill catalog with aliases, structured parsed fields, relational skill links, file-size/type checks (5 MB maximum), private-path storage, and candidate/HR access controls.
4. **NLP and matching:** FastAPI parsing, English stop-word removal and deterministic inflection normalization for TF-IDF/cosine similarity, canonical skill overlap, experience and education components, configurable weighted scoring, matched/missing skills, section feedback, and improvement tips. Protected demographic/name/photo text is excluded from the candidate scoring text.
5. **Jobs and applications:** HR job create/edit/deactivate and skill assignment, candidate recommendations, resume-owner-checked applications, candidate application tracking, recruiter candidate ranking, and application status changes.
6. **Employee and attrition analytics:** HR employee CRUD and CSV import; a synthetic data generator (2,000 rows by default); Random Forest, Logistic Regression, and Gradient Boosting comparison; class-imbalance weighting; accuracy, precision, recall, F1, ROC-AUC, and confusion matrix output; persisted predictions; department risk summaries; and a high-risk follow-up list.
7. **Reports and administration:** PDF match report download, system statistics, role/user activation management, and authenticated API activity audit records (request metadata only; request bodies are not logged).
8. **Frontend and deployment:** React 18/Vite/Tailwind/Axios/React Router interface with candidate, HR, and admin pages, plus Compose services for MySQL, Spring Boot, FastAPI, and the static frontend.

## Quick start: Docker Compose

Docker Engine and the Compose plugin are required. The compose file refuses to start with missing database or JWT secrets.

```bash
cp .env.example .env
# Edit .env: replace both DB password placeholders and set a random JWT_SECRET (32+ characters).
docker compose up --build
```

Open:

- Frontend: <http://localhost:5173>
- Spring API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- FastAPI docs: <http://localhost:8000/docs>
- ML health: <http://localhost:8000/health>

Stop containers while preserving local data with `docker compose down`. MySQL, uploads, generated ML data, and the trained model use named volumes. Do not use `docker compose down -v` unless intentionally deleting that local data.

## Run each service directly

### 1. MySQL 8

Create the database and a least-privilege local account using a privileged MySQL session:

```sql
CREATE DATABASE hiresense CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE USER 'hiresense'@'localhost' IDENTIFIED BY 'choose-a-unique-password';
GRANT ALL PRIVILEGES ON hiresense.* TO 'hiresense'@'localhost';
```

`schema.sql` and `data.sql` run at Spring startup. Seed accounts/data are for development only.

### 2. Python ML/NLP service

Use Python 3.10 or newer:

```bash
cd ml-service
python3 -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

The model trains lazily on generated synthetic records if the configured model file is missing. For explicit training:

```bash
python scripts/generate_synthetic_data.py --records 2000 --output data/attrition_synthetic.csv
python scripts/train_attrition.py --records 2000 --data data/attrition_synthetic.csv --model models/attrition_model.joblib
```

### 3. Spring Boot API

Set credentials and the JWT secret; do not commit them:

```bash
export DB_URL='jdbc:mysql://localhost:3306/hiresense?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
export DB_USERNAME='hiresense'
export DB_PASSWORD='choose-the-same-local-password'
export JWT_SECRET='replace-with-a-random-secret-of-at-least-32-characters'
export ML_SERVICE_URL='http://localhost:8000'
export RESUME_STORAGE_DIR="$PWD/data/resumes"
cd backend
mvn test
mvn spring-boot:run
```

For tests, the `test` Spring profile uses an isolated H2 database and a test-only signing key; it does not run the MySQL initialization scripts.

### 4. React frontend

```bash
cd frontend
npm ci
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

For production assets, run `npm run build`; configure the API URL at build time using `VITE_API_BASE_URL`.

## Development sign-in accounts

The MySQL seed inserts the following local-only accounts. The shared password is `password`:

- Admin: `admin@hiresense.local`
- HR: `hr1@hiresense.local`, `hr2@hiresense.local`
- Candidates: `candidate1@hiresense.local` through `candidate5@hiresense.local`

Change or remove seed credentials before any non-local use. Public registration creates candidates only; HR/Admin access should be provisioned by an administrator to prevent privilege escalation.

## REST API overview

All application routes require a valid bearer JWT unless otherwise noted. See Swagger UI for schemas, validation, and full endpoint details.

| Module | Main endpoints |
|---|---|
| Authentication | `POST /api/auth/register`, `POST /api/auth/login` |
| Profile | `GET/PUT /api/profile/me`, `GET /api/profile/resumes` |
| Resumes | `POST /api/resumes/upload`, `GET /api/resumes`, `GET /api/resumes/{id}` |
| Jobs/applications | `GET/POST /api/jobs`, `GET/PUT/DELETE /api/jobs/{id}`, `PUT /api/jobs/{id}/skills`, `POST /api/jobs/{id}/applications`, `GET /api/jobs/applications/me`, `PATCH /api/jobs/applications/{applicationId}/status` |
| Matching | `POST /api/match` with `{ "resumeId": 1, "jobId": 1 }`, `GET /api/jobs/recommended`, `GET /api/jobs/{id}/candidates` |
| PDF report | `GET /api/reports/match/{matchId}/pdf` |
| Employees | `GET/POST /api/employees`, `GET/PUT/DELETE /api/employees/{id}`, `POST /api/employees/import` |
| Attrition | `POST /api/attrition/predict/{employeeId}`, `POST /api/attrition/train`, `GET /api/attrition/dashboard` |
| Admin | `GET /api/admin/users`, `PATCH /api/admin/users/{id}/active?value=true`, `GET /api/admin/stats`, `GET /api/admin/activity` |
| Python service | `GET /health`, `POST /nlp/parse`, `POST /match`, `POST /attrition/predict`, `POST /attrition/train` |

## Tests and verification

```bash
# Java backend / JPA mappings
cd backend && mvn test

# Python service
cd ml-service && python3 -m pytest -q

# React production build
cd frontend && npm run build
```

**Verified in this build (2 October 2026):** 4 Java/JUnit tests and 4 Python/pytest tests passed; `npm run build` succeeded with Vite's advisory about the 706 kB main chunk. A live MySQL/FastAPI/Spring smoke test exercised candidate/HR/admin login, synthetic PDF upload and parsing, matching with saved section feedback, PDF report download, application tracking, recruiter ranking, attrition prediction/dashboard, and admin stats/activity. MySQL contained 8 seeded users, 10 jobs, and 50 synthetic employees at pre-test verification. Synthetic smoke records were removed afterward. Docker Compose could not be built or started because Docker was unavailable; run the documented Compose commands in a Docker-enabled environment.

## Security, fairness, and known limitations

- Set `DB_PASSWORD`, `MYSQL_ROOT_PASSWORD`, and `JWT_SECRET` in a local `.env` or process environment. `.env` is ignored by Git. Never use the example placeholders in a shared or production deployment.
- The backend enforces a 5 MB upload limit and PDF/DOCX extensions, authenticates resume access, and stores file data outside public web roots. The sample setup does **not** provide HTTPS termination or encryption at rest; use TLS and encrypted volumes/storage in deployment.
- User names, gender, age, and photo are not intended as candidate matching features. Matching receives job-related resume text with identity/demographic terms stripped, plus normalized skill/experience/education data. This does not remove proxy bias; test subgroup outcomes before real use.
- Attrition training uses **synthetic generated labels**, not validated HR outcomes. Some employee predictors are mapped to proxies or neutral defaults where the input database has no direct equivalent; top factors are heuristic and are not causal explanations. Do not use results to automate punitive employment actions.
- Activity auditing records authenticated successful API method/path/status/IP metadata and actor identity, not request bodies. Configure retention and access policies before deployment.
- Resume deletion/retention workflows, production-grade object storage, key rotation, encrypted persistence, model calibration/fairness evaluation, external identity providers, and a background task queue are not included.
- The active build sandbox did not have Docker installed, so the Compose stack itself could not be launched here. Java, Python, frontend, and the live MySQL/FastAPI/Spring API paths were verified separately.

## Academic report and SDGs

See [the academic design report](docs/academic-report.md) for Use Case, Class, Sequence, Activity, Component, State Machine, and Deployment diagrams, representative synthetic records/output, and a test-case matrix. [The SDG map](docs/sdg-feature-map.md) relates features to SDG 4 (Quality Education), SDG 5 (Gender Equality), SDG 8 (Decent Work and Economic Growth), SDG 9 (Industry, Innovation and Infrastructure), and SDG 10 (Reduced Inequalities). These are intended alignments—not claims of measured social impact.
