-- Development-only sample data; every demo account shares password 'password'. Replace before deployment.
INSERT INTO roles (id, name, description) VALUES
(1, 'ADMIN', 'System administrator'), (2, 'HR', 'Recruiter and workforce analyst'), (3, 'CANDIDATE', 'Job seeker')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO users (id, full_name, email, password_hash, active) VALUES
(1, 'HireSense Admin', 'admin@hiresense.local', '$2b$10$r8RWEoGu6jrXMuV.Vs6nzu1Y/uA6HiDHN0VMzgha/oTc.aNgAIP0O', TRUE),
(2, 'Avery Recruiter', 'hr1@hiresense.local', '$2b$10$r8RWEoGu6jrXMuV.Vs6nzu1Y/uA6HiDHN0VMzgha/oTc.aNgAIP0O', TRUE),
(3, 'Morgan Recruiter', 'hr2@hiresense.local', '$2b$10$r8RWEoGu6jrXMuV.Vs6nzu1Y/uA6HiDHN0VMzgha/oTc.aNgAIP0O', TRUE),
(4, 'Alex Candidate', 'candidate1@hiresense.local', '$2b$10$r8RWEoGu6jrXMuV.Vs6nzu1Y/uA6HiDHN0VMzgha/oTc.aNgAIP0O', TRUE),
(5, 'Blair Candidate', 'candidate2@hiresense.local', '$2b$10$r8RWEoGu6jrXMuV.Vs6nzu1Y/uA6HiDHN0VMzgha/oTc.aNgAIP0O', TRUE),
(6, 'Casey Candidate', 'candidate3@hiresense.local', '$2b$10$r8RWEoGu6jrXMuV.Vs6nzu1Y/uA6HiDHN0VMzgha/oTc.aNgAIP0O', TRUE),
(7, 'Devon Candidate', 'candidate4@hiresense.local', '$2b$10$r8RWEoGu6jrXMuV.Vs6nzu1Y/uA6HiDHN0VMzgha/oTc.aNgAIP0O', TRUE),
(8, 'Elliot Candidate', 'candidate5@hiresense.local', '$2b$10$r8RWEoGu6jrXMuV.Vs6nzu1Y/uA6HiDHN0VMzgha/oTc.aNgAIP0O', TRUE)
ON DUPLICATE KEY UPDATE email = VALUES(email);

INSERT INTO user_roles (user_id, role_id) VALUES (1,1), (2,2), (3,2), (4,3), (5,3), (6,3), (7,3), (8,3)
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

INSERT INTO candidate_profiles (id, user_id, phone, location, headline, summary, years_experience, highest_education) VALUES
(1, 4, '+1-555-0101', 'Austin, TX', 'Java developer', 'Backend engineer focused on reliable services.', 4.0, 'Bachelors'),
(2, 5, '+1-555-0102', 'Denver, CO', 'Data analyst', 'Analyst experienced in SQL and dashboards.', 3.0, 'Bachelors'),
(3, 6, '+1-555-0103', 'Seattle, WA', 'Frontend engineer', 'React developer with product UI experience.', 5.0, 'Bachelors'),
(4, 7, '+1-555-0104', 'Boston, MA', 'ML engineer', 'Machine learning practitioner with Python experience.', 6.0, 'Masters'),
(5, 8, '+1-555-0105', 'Chicago, IL', 'Project manager', 'Technical program and people leadership.', 8.0, 'Masters')
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

INSERT INTO skills (id, name, category, description) VALUES
(1, 'Java', 'TECHNICAL', 'Development sample skill'),
(2, 'Spring Boot', 'TECHNICAL', 'Development sample skill'),
(3, 'SQL', 'TECHNICAL', 'Development sample skill'),
(4, 'Python', 'TECHNICAL', 'Development sample skill'),
(5, 'FastAPI', 'TECHNICAL', 'Development sample skill'),
(6, 'React', 'TECHNICAL', 'Development sample skill'),
(7, 'Communication', 'SOFT', 'Development sample skill'),
(8, 'Leadership', 'SOFT', 'Development sample skill'),
(9, 'Machine Learning', 'TECHNICAL', 'Development sample skill'),
(10, 'Docker', 'TECHNICAL', 'Development sample skill'),
(11, 'Analytics', 'TECHNICAL', 'Development sample skill'),
(12, 'Conflict Resolution', 'SOFT', 'Development sample skill')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO jobs (id, created_by_user_id, title, description, min_experience_years, education_level, salary_min, salary_max, location, active) VALUES
(1, 2, 'Senior Java Engineer', 'Build backend services with Java and Spring Boot.', 4, 'Bachelors', 95000, 145000, 'Austin, TX', TRUE),
(2, 3, 'Data Analyst', 'Analyze product and business data using SQL and Python.', 2, 'Bachelors', 70000, 105000, 'Denver, CO', TRUE),
(3, 2, 'Frontend Engineer', 'Develop responsive user experiences with React.', 3, 'Bachelors', 85000, 130000, 'Seattle, WA', TRUE),
(4, 3, 'Machine Learning Engineer', 'Develop production ML systems using Python.', 4, 'Masters', 115000, 175000, 'Boston, MA', TRUE),
(5, 2, 'Engineering Manager', 'Lead engineering teams and delivery.', 8, 'Bachelors', 140000, 200000, 'Chicago, IL', TRUE),
(6, 3, 'Backend Developer', 'Create API services and database integrations.', 2, 'Bachelors', 80000, 125000, 'Remote', TRUE),
(7, 2, 'People Analytics Specialist', 'Build workforce analytics with SQL and statistics.', 3, 'Bachelors', 78000, 118000, 'New York, NY', TRUE),
(8, 3, 'Cloud Platform Engineer', 'Operate containerized cloud infrastructure.', 4, 'Bachelors', 105000, 160000, 'Remote', TRUE),
(9, 2, 'Product Data Scientist', 'Apply machine learning and analytics to product data.', 3, 'Masters', 100000, 155000, 'San Francisco, CA', TRUE),
(10, 3, 'Technical Project Manager', 'Coordinate technical programs and communicate delivery status.', 5, 'Bachelors', 110000, 165000, 'Chicago, IL', TRUE)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO job_skills (id, job_id, skill_id, skill_type, importance) VALUES
(1, 1, 1, 'REQUIRED', 1),
(2, 1, 2, 'REQUIRED', 1),
(3, 1, 3, 'PREFERRED', 1),
(4, 2, 3, 'REQUIRED', 1),
(5, 2, 4, 'PREFERRED', 1),
(6, 2, 11, 'REQUIRED', 1),
(7, 3, 6, 'REQUIRED', 1),
(8, 3, 7, 'PREFERRED', 1),
(9, 4, 4, 'REQUIRED', 1),
(10, 4, 9, 'REQUIRED', 1),
(11, 4, 5, 'PREFERRED', 1),
(12, 5, 8, 'REQUIRED', 1),
(13, 5, 7, 'PREFERRED', 1),
(14, 6, 1, 'REQUIRED', 1),
(15, 6, 2, 'PREFERRED', 1),
(16, 7, 3, 'REQUIRED', 1),
(17, 7, 11, 'REQUIRED', 1),
(18, 7, 7, 'PREFERRED', 1),
(19, 8, 10, 'REQUIRED', 1),
(20, 8, 1, 'PREFERRED', 1),
(21, 9, 4, 'REQUIRED', 1),
(22, 9, 9, 'REQUIRED', 1),
(23, 9, 11, 'PREFERRED', 1),
(24, 10, 7, 'REQUIRED', 1),
(25, 10, 8, 'PREFERRED', 1)
ON DUPLICATE KEY UPDATE importance = VALUES(importance);

INSERT INTO employees (id, employee_code, full_name, age, department, job_role, business_travel, monthly_income, years_at_company, total_experience_years, job_satisfaction, work_life_balance, performance_rating, overtime, distance_from_home_km, promotions, training_times_last_year, years_in_current_role, years_since_last_promotion, years_with_current_manager, num_companies_worked, stock_option_level, percent_salary_hike, attrition) VALUES
(1, 'HSE0001', 'Jordan Morgan', 30, 'Engineering', 'Software Engineer', 'Non-Travel', 3751, 2, 3, 2, 4, 3, FALSE, 7, 1, 2, 2, 1, 2, 2, 1, 12, FALSE),
(2, 'HSE0002', 'Taylor Morgan', 37, 'Data', 'Data Analyst', 'Travel_Frequently', 4002, 3, 4, 3, 3, 3, FALSE, 12, 2, 3, 3, 2, 3, 3, 2, 14, FALSE),
(3, 'HSE0003', 'Riley Reese', 44, 'Product', 'Product Manager', 'Travel_Rarely', 4253, 4, 5, 4, 2, 3, TRUE, 17, 3, 4, 4, 3, 4, 4, 3, 16, FALSE),
(4, 'HSE0004', 'Cameron Reese', 51, 'People', 'HR Analyst', 'Non-Travel', 4504, 5, 6, 1, 1, 3, FALSE, 22, 0, 5, 5, 4, 5, 5, 0, 18, FALSE),
(5, 'HSE0005', 'Quinn Hayes', 24, 'Operations', 'Platform Engineer', 'Travel_Frequently', 4755, 6, 7, 2, 4, 3, FALSE, 27, 1, 6, 6, 5, 6, 6, 1, 20, TRUE),
(6, 'HSE0006', 'Parker Hayes', 31, 'Engineering', 'Software Engineer', 'Travel_Rarely', 5006, 7, 8, 3, 3, 4, TRUE, 32, 2, 1, 7, 6, 7, 1, 2, 22, FALSE),
(7, 'HSE0007', 'Robin Brooks', 38, 'Data', 'Data Analyst', 'Non-Travel', 5257, 8, 9, 4, 2, 3, FALSE, 2, 3, 2, 8, 0, 8, 2, 3, 24, FALSE),
(8, 'HSE0008', 'Drew Brooks', 45, 'Product', 'Product Manager', 'Travel_Frequently', 5508, 9, 10, 1, 1, 3, FALSE, 7, 0, 3, 1, 1, 9, 3, 0, 26, FALSE),
(9, 'HSE0009', 'Jamie Lane', 52, 'People', 'HR Analyst', 'Travel_Rarely', 5759, 10, 11, 2, 4, 3, TRUE, 12, 1, 4, 2, 2, 1, 4, 1, 10, FALSE),
(10, 'HSE0010', 'Kendall Lane', 25, 'Operations', 'Platform Engineer', 'Non-Travel', 6010, 11, 12, 3, 3, 3, FALSE, 17, 2, 5, 3, 3, 2, 5, 2, 12, TRUE),
(11, 'HSE0011', 'Jordan Murphy', 32, 'Engineering', 'Software Engineer', 'Travel_Frequently', 6261, 12, 13, 4, 2, 3, FALSE, 22, 3, 6, 4, 4, 3, 6, 3, 14, FALSE),
(12, 'HSE0012', 'Taylor Murphy', 39, 'Data', 'Data Analyst', 'Travel_Rarely', 6512, 1, 14, 1, 1, 4, TRUE, 27, 0, 1, 5, 5, 4, 1, 0, 16, FALSE),
(13, 'HSE0013', 'Riley Reed', 46, 'Product', 'Product Manager', 'Non-Travel', 6763, 2, 15, 2, 4, 3, FALSE, 32, 1, 2, 6, 6, 5, 2, 1, 18, FALSE),
(14, 'HSE0014', 'Cameron Reed', 53, 'People', 'HR Analyst', 'Travel_Frequently', 7014, 3, 16, 3, 3, 3, FALSE, 2, 2, 3, 7, 0, 6, 3, 2, 20, TRUE),
(15, 'HSE0015', 'Quinn Ellis', 26, 'Operations', 'Platform Engineer', 'Travel_Rarely', 7265, 4, 17, 4, 2, 3, TRUE, 7, 3, 4, 8, 1, 7, 4, 3, 22, TRUE),
(16, 'HSE0016', 'Parker Ellis', 33, 'Engineering', 'Software Engineer', 'Non-Travel', 7516, 5, 18, 1, 1, 3, FALSE, 12, 0, 5, 1, 2, 8, 5, 0, 24, FALSE),
(17, 'HSE0017', 'Robin Bennett', 40, 'Data', 'Data Analyst', 'Travel_Frequently', 7767, 6, 19, 2, 4, 3, FALSE, 17, 1, 6, 2, 3, 9, 6, 1, 26, FALSE),
(18, 'HSE0018', 'Drew Bennett', 47, 'Product', 'Product Manager', 'Travel_Rarely', 8018, 7, 20, 3, 3, 4, TRUE, 22, 2, 1, 3, 4, 1, 1, 2, 10, FALSE),
(19, 'HSE0019', 'Jamie Shaw', 54, 'People', 'HR Analyst', 'Non-Travel', 8269, 8, 21, 4, 2, 3, FALSE, 27, 3, 2, 4, 5, 2, 2, 3, 12, FALSE),
(20, 'HSE0020', 'Kendall Shaw', 27, 'Operations', 'Platform Engineer', 'Travel_Frequently', 8520, 9, 22, 1, 1, 3, FALSE, 32, 0, 3, 5, 6, 3, 3, 0, 14, TRUE),
(21, 'HSE0021', 'Jordan Morgan', 34, 'Engineering', 'Software Engineer', 'Travel_Rarely', 8771, 10, 23, 2, 4, 3, TRUE, 2, 1, 4, 6, 0, 4, 4, 1, 16, FALSE),
(22, 'HSE0022', 'Taylor Morgan', 41, 'Data', 'Data Analyst', 'Non-Travel', 9022, 11, 2, 3, 3, 3, FALSE, 7, 2, 5, 7, 1, 5, 5, 2, 18, FALSE),
(23, 'HSE0023', 'Riley Reese', 48, 'Product', 'Product Manager', 'Travel_Frequently', 9273, 12, 3, 4, 2, 3, FALSE, 12, 3, 6, 8, 2, 6, 6, 3, 20, FALSE),
(24, 'HSE0024', 'Cameron Reese', 55, 'People', 'HR Analyst', 'Travel_Rarely', 9524, 1, 4, 1, 1, 4, TRUE, 17, 0, 1, 1, 3, 7, 1, 0, 22, FALSE),
(25, 'HSE0025', 'Quinn Hayes', 28, 'Operations', 'Platform Engineer', 'Non-Travel', 9775, 2, 5, 2, 4, 3, FALSE, 22, 1, 2, 2, 4, 8, 2, 1, 24, TRUE),
(26, 'HSE0026', 'Parker Hayes', 35, 'Engineering', 'Software Engineer', 'Travel_Frequently', 10026, 3, 6, 3, 3, 3, FALSE, 27, 2, 3, 3, 5, 9, 3, 2, 26, FALSE),
(27, 'HSE0027', 'Robin Brooks', 42, 'Data', 'Data Analyst', 'Travel_Rarely', 10277, 4, 7, 4, 2, 3, TRUE, 32, 3, 4, 4, 6, 1, 4, 3, 10, FALSE),
(28, 'HSE0028', 'Drew Brooks', 49, 'Product', 'Product Manager', 'Non-Travel', 10528, 5, 8, 1, 1, 3, FALSE, 2, 0, 5, 5, 0, 2, 5, 0, 12, TRUE),
(29, 'HSE0029', 'Jamie Lane', 56, 'People', 'HR Analyst', 'Travel_Frequently', 10779, 6, 9, 2, 4, 3, FALSE, 7, 1, 6, 6, 1, 3, 6, 1, 14, FALSE),
(30, 'HSE0030', 'Kendall Lane', 29, 'Operations', 'Platform Engineer', 'Travel_Rarely', 11030, 7, 10, 3, 3, 4, TRUE, 12, 2, 1, 7, 2, 4, 1, 2, 16, TRUE),
(31, 'HSE0031', 'Jordan Murphy', 36, 'Engineering', 'Software Engineer', 'Non-Travel', 11281, 8, 11, 4, 2, 3, FALSE, 17, 3, 2, 8, 3, 5, 2, 3, 18, FALSE),
(32, 'HSE0032', 'Taylor Murphy', 43, 'Data', 'Data Analyst', 'Travel_Frequently', 11532, 9, 12, 1, 1, 3, FALSE, 22, 0, 3, 1, 4, 6, 3, 0, 20, FALSE),
(33, 'HSE0033', 'Riley Reed', 50, 'Product', 'Product Manager', 'Travel_Rarely', 11783, 10, 13, 2, 4, 3, TRUE, 27, 1, 4, 2, 5, 7, 4, 1, 22, FALSE),
(34, 'HSE0034', 'Cameron Reed', 23, 'People', 'HR Analyst', 'Non-Travel', 12034, 11, 14, 3, 3, 3, FALSE, 32, 2, 5, 3, 6, 8, 5, 2, 24, FALSE),
(35, 'HSE0035', 'Quinn Ellis', 30, 'Operations', 'Platform Engineer', 'Travel_Frequently', 12285, 12, 15, 4, 2, 3, FALSE, 2, 3, 6, 4, 0, 9, 6, 3, 26, TRUE),
(36, 'HSE0036', 'Parker Ellis', 37, 'Engineering', 'Software Engineer', 'Travel_Rarely', 3536, 1, 16, 1, 1, 4, TRUE, 7, 0, 1, 5, 1, 1, 1, 0, 10, FALSE),
(37, 'HSE0037', 'Robin Bennett', 44, 'Data', 'Data Analyst', 'Non-Travel', 3787, 2, 17, 2, 4, 3, FALSE, 12, 1, 2, 6, 2, 2, 2, 1, 12, FALSE),
(38, 'HSE0038', 'Drew Bennett', 51, 'Product', 'Product Manager', 'Travel_Frequently', 4038, 3, 18, 3, 3, 3, FALSE, 17, 2, 3, 7, 3, 3, 3, 2, 14, FALSE),
(39, 'HSE0039', 'Jamie Shaw', 24, 'People', 'HR Analyst', 'Travel_Rarely', 4289, 4, 19, 4, 2, 3, TRUE, 22, 3, 4, 8, 4, 4, 4, 3, 16, FALSE),
(40, 'HSE0040', 'Kendall Shaw', 31, 'Operations', 'Platform Engineer', 'Non-Travel', 4540, 5, 20, 1, 1, 3, FALSE, 27, 0, 5, 1, 5, 5, 5, 0, 18, TRUE),
(41, 'HSE0041', 'Jordan Morgan', 38, 'Engineering', 'Software Engineer', 'Travel_Frequently', 4791, 6, 21, 2, 4, 3, FALSE, 32, 1, 6, 2, 6, 6, 6, 1, 20, FALSE),
(42, 'HSE0042', 'Taylor Morgan', 45, 'Data', 'Data Analyst', 'Travel_Rarely', 5042, 7, 22, 3, 3, 4, TRUE, 2, 2, 1, 3, 0, 7, 1, 2, 22, TRUE),
(43, 'HSE0043', 'Riley Reese', 52, 'Product', 'Product Manager', 'Non-Travel', 5293, 8, 23, 4, 2, 3, FALSE, 7, 3, 2, 4, 1, 8, 2, 3, 24, FALSE),
(44, 'HSE0044', 'Cameron Reese', 25, 'People', 'HR Analyst', 'Travel_Frequently', 5544, 9, 2, 1, 1, 3, FALSE, 12, 0, 3, 5, 2, 9, 3, 0, 26, FALSE),
(45, 'HSE0045', 'Quinn Hayes', 32, 'Operations', 'Platform Engineer', 'Travel_Rarely', 5795, 10, 3, 2, 4, 3, TRUE, 17, 1, 4, 6, 3, 1, 4, 1, 10, TRUE),
(46, 'HSE0046', 'Parker Hayes', 39, 'Engineering', 'Software Engineer', 'Non-Travel', 6046, 11, 4, 3, 3, 3, FALSE, 22, 2, 5, 7, 4, 2, 5, 2, 12, FALSE),
(47, 'HSE0047', 'Robin Brooks', 46, 'Data', 'Data Analyst', 'Travel_Frequently', 6297, 12, 5, 4, 2, 3, FALSE, 27, 3, 6, 8, 5, 3, 6, 3, 14, FALSE),
(48, 'HSE0048', 'Drew Brooks', 53, 'Product', 'Product Manager', 'Travel_Rarely', 6548, 1, 6, 1, 1, 4, TRUE, 32, 0, 1, 1, 6, 4, 1, 0, 16, FALSE),
(49, 'HSE0049', 'Jamie Lane', 26, 'People', 'HR Analyst', 'Non-Travel', 6799, 2, 7, 2, 4, 3, FALSE, 2, 1, 2, 2, 0, 5, 2, 1, 18, FALSE),
(50, 'HSE0050', 'Kendall Lane', 33, 'Operations', 'Platform Engineer', 'Travel_Frequently', 7050, 3, 8, 3, 3, 3, FALSE, 7, 2, 3, 3, 1, 6, 3, 2, 20, TRUE)
ON DUPLICATE KEY UPDATE employee_code = VALUES(employee_code);
