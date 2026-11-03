# Phase 1 — Database and persistence layer

## Included

Phase 1 contains MySQL 8 DDL for users and roles, candidate profiles, resumes and parsed data, skills, jobs and job skills, applications, match results, employees, attrition predictions, and activity logs. It includes JPA entities, enums, Spring Data repositories, development seed data (1 admin, 2 HR users, 5 candidate users, 10 jobs, 50 employees), and an H2-backed mapping test.

## Relationship overview

`users` and `roles` are many-to-many through `user_roles`. A candidate user has one candidate profile; a candidate profile can have multiple resumes, each with optional parsed data. A job has multiple job-skill rows, each pointing to a normalized skill. Candidate profiles, jobs, and resumes are referenced by applications and match results. Employees may have multiple historical attrition predictions. Activity-log actors are nullable to allow system-originated events.

## Persistence and fairness notes

Indexes and foreign keys are defined in `schema.sql`. Account deactivation is represented by `users.active`; associated business records are not implicitly deleted. The persistence layer does not itself make hiring decisions. Candidate ranking must not consume candidate name, gender, age, or photo. The database may retain employee age because it is requested for workforce records, but downstream attrition features need a separately reviewed and documented feature policy.
