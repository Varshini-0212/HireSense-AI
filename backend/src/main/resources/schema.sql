SET NAMES utf8mb4;

CREATE TABLE IF NOT EXISTS roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_roles_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(190) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_users_email UNIQUE (email),
    INDEX idx_users_active (active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    INDEX idx_user_roles_role (role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS candidate_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    phone VARCHAR(32),
    location VARCHAR(160),
    headline VARCHAR(180),
    summary TEXT,
    years_experience DECIMAL(4,1),
    highest_education VARCHAR(120),
    portfolio_url VARCHAR(500),
    linkedin_url VARCHAR(500),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_candidate_profile_user UNIQUE (user_id),
    CONSTRAINT fk_candidate_profiles_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS resumes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    candidate_profile_id BIGINT NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    storage_path VARCHAR(600) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size_bytes BIGINT NOT NULL,
    sha256 VARCHAR(64),
    status VARCHAR(20) NOT NULL,
    uploaded_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_resumes_candidate_uploaded (candidate_profile_id, uploaded_at),
    CONSTRAINT fk_resumes_candidate FOREIGN KEY (candidate_profile_id) REFERENCES candidate_profiles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS skills (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(40) NOT NULL,
    description VARCHAR(255),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_skills_name UNIQUE (name),
    INDEX idx_skills_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS parsed_resume_data (
    id BIGINT NOT NULL AUTO_INCREMENT,
    resume_id BIGINT NOT NULL,
    full_text LONGTEXT,
    extracted_name VARCHAR(160),
    extracted_email VARCHAR(190),
    extracted_phone VARCHAR(40),
    years_experience DECIMAL(4,1),
    skills_json JSON NOT NULL,
    education_json JSON,
    certifications_json JSON,
    projects_json JSON,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_parsed_resume_resume UNIQUE (resume_id),
    CONSTRAINT fk_parsed_resume_resume FOREIGN KEY (resume_id) REFERENCES resumes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS parsed_resume_skills (
    parsed_resume_data_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    PRIMARY KEY (parsed_resume_data_id, skill_id),
    INDEX idx_parsed_resume_skills_skill (skill_id),
    CONSTRAINT fk_parsed_resume_skills_data FOREIGN KEY (parsed_resume_data_id) REFERENCES parsed_resume_data(id),
    CONSTRAINT fk_parsed_resume_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS jobs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_by_user_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    min_experience_years DECIMAL(4,1),
    education_level VARCHAR(120),
    salary_min DECIMAL(12,2),
    salary_max DECIMAL(12,2),
    location VARCHAR(160),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_jobs_active_created (active, created_at),
    INDEX idx_jobs_owner (created_by_user_id),
    CONSTRAINT fk_jobs_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS job_skills (
    id BIGINT NOT NULL AUTO_INCREMENT,
    job_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    skill_type VARCHAR(20) NOT NULL,
    importance INT NOT NULL DEFAULT 1,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_job_skill_type UNIQUE (job_id, skill_id, skill_type),
    INDEX idx_job_skills_skill (skill_id),
    CONSTRAINT fk_job_skills_job FOREIGN KEY (job_id) REFERENCES jobs(id),
    CONSTRAINT fk_job_skills_skill FOREIGN KEY (skill_id) REFERENCES skills(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS applications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    candidate_profile_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    resume_id BIGINT,
    status VARCHAR(24) NOT NULL,
    applied_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_application_candidate_job UNIQUE (candidate_profile_id, job_id),
    INDEX idx_applications_job_status (job_id, status),
    INDEX idx_applications_candidate_applied (candidate_profile_id, applied_at),
    CONSTRAINT fk_applications_candidate FOREIGN KEY (candidate_profile_id) REFERENCES candidate_profiles(id),
    CONSTRAINT fk_applications_job FOREIGN KEY (job_id) REFERENCES jobs(id),
    CONSTRAINT fk_applications_resume FOREIGN KEY (resume_id) REFERENCES resumes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS match_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    resume_id BIGINT NOT NULL,
    job_id BIGINT NOT NULL,
    application_id BIGINT,
    overall_score DECIMAL(5,2) NOT NULL,
    semantic_score DECIMAL(5,2),
    skill_score DECIMAL(5,2),
    experience_score DECIMAL(5,2),
    education_score DECIMAL(5,2),
    matched_skills_json JSON NOT NULL,
    missing_skills_json JSON NOT NULL,
    feedback_json JSON NOT NULL,
    improvement_tips_json JSON NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_match_job_score (job_id, overall_score),
    INDEX idx_match_resume_job_created (resume_id, job_id, created_at),
    CONSTRAINT fk_match_resume FOREIGN KEY (resume_id) REFERENCES resumes(id),
    CONSTRAINT fk_match_job FOREIGN KEY (job_id) REFERENCES jobs(id),
    CONSTRAINT fk_match_application FOREIGN KEY (application_id) REFERENCES applications(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS employees (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_code VARCHAR(40) NOT NULL,
    full_name VARCHAR(160) NOT NULL,
    age INT,
    department VARCHAR(100) NOT NULL,
    job_role VARCHAR(120) NOT NULL,
    business_travel VARCHAR(40),
    monthly_income DECIMAL(12,2),
    years_at_company INT,
    total_experience_years INT,
    job_satisfaction INT,
    work_life_balance INT,
    performance_rating INT,
    overtime BOOLEAN NOT NULL DEFAULT FALSE,
    distance_from_home_km INT,
    promotions INT,
    training_times_last_year INT,
    years_in_current_role INT,
    years_since_last_promotion INT,
    years_with_current_manager INT,
    num_companies_worked INT,
    stock_option_level INT,
    percent_salary_hike INT,
    attrition BOOLEAN,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_employees_code UNIQUE (employee_code),
    INDEX idx_employee_department (department),
    INDEX idx_employee_attrition (attrition)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS attrition_predictions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    employee_id BIGINT NOT NULL,
    attrition_probability DECIMAL(6,5) NOT NULL,
    risk_level VARCHAR(12) NOT NULL,
    top_factors_json JSON NOT NULL,
    suggested_actions_json JSON NOT NULL,
    model_version VARCHAR(80),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_attrition_employee_created (employee_id, created_at),
    INDEX idx_attrition_risk_created (risk_level, created_at),
    CONSTRAINT fk_attrition_employee FOREIGN KEY (employee_id) REFERENCES employees(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    actor_user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(80),
    entity_id VARCHAR(80),
    details_json JSON,
    ip_address VARCHAR(45),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_activity_created_at (created_at),
    INDEX idx_activity_actor_created (actor_user_id, created_at),
    CONSTRAINT fk_activity_actor FOREIGN KEY (actor_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
