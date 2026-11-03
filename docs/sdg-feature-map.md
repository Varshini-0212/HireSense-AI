# Sustainable Development Goals and HireSense AI

This map describes the **intended feature alignment** of the implemented HireSense prototype. It is not a claim of measured social impact, verified fairness, or suitability for operational employment decisions.

| SDG | Implemented feature alignment | Intended contribution and limitation |
|---|---|---|
| **SDG 4 — Quality Education** | Resume parsing, visible job-related match components, skill-gap feedback, and learning/certification suggestions | Helps candidates identify relevant skills and learning opportunities. Suggestions are not certifications, and access to training remains outside the system. |
| **SDG 5 — Gender Equality** | Candidate scoring is designed to exclude direct gender input; match feedback and audit records support review | Removing one direct input does not guarantee fairness. Proxy variables and selection outcomes require paired tests and protected-group evaluation before real use. |
| **SDG 8 — Decent Work and Economic Growth** | Active job listings, application tracking, explainable recruiter ranking, and supportive attrition-risk review | Can improve opportunity visibility and surface retention needs. Attrition prediction uses synthetic training data and must not replace human review or trigger punitive action. |
| **SDG 9 — Industry, Innovation and Infrastructure** | Modular Java/Spring, Python/FastAPI, React, MySQL, API tests, and reproducible model-training code | Demonstrates a maintainable digital architecture. The local Docker Compose profile is supplied but was not run in the build sandbox; reliability, privacy, security, and monitoring remain deployment responsibilities. |
| **SDG 10 — Reduced Inequalities** | Skills/qualification-centered comparison, missing-skill explanations, candidate application tracking, and role-based access | Focuses comparison on job-related qualifications. Proxy bias, unequal data quality, accessibility barriers, and disparate outcomes may remain. |

## Guardrails and remaining evidence needs

- Keep candidate name, gender, age, and photo out of ranking inputs; this prototype implements exclusion/stripping controls, but no paired counterfactual fairness test was performed in the final verification.
- Show match components, matched/missing skills, and feedback to support human review; never treat a score as an automatic hiring decision.
- Treat attrition probability as a signal for supportive, confidential review only. The included model uses synthetic labels and is not validated on real workforce outcomes.
- Validate model calibration, false-positive/false-negative rates, subgroup outcomes, accessibility, privacy, retention, and security before operational deployment.
