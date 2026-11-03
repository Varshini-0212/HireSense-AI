"""Curated, dependency-free skills catalog for resume/job matching.

The catalog deliberately contains canonical names plus common spelling and acronym aliases.
It is data, not a corpus download, so service startup is deterministic and offline-capable.
"""


def _items(value: str):
    return [item.strip().lower() for item in value.split(",") if item.strip()]


TECHNICAL = _items("""
python, r, java, javascript, typescript, c, c++, c#, go, rust, kotlin, swift, scala, php, ruby, perl, dart, matlab, julia, groovy, bash, powershell, sql, pl/sql, t-sql, html, css, sass, less, xml, json, yaml, graphql, rest api, grpc, websocket, oauth, openapi, microservices, api design, system design, object oriented programming, functional programming, data structures, algorithms, design patterns, clean code, test driven development, domain driven design, react, angular, vue.js, svelte, next.js, nuxt.js, node.js, express.js, nestjs, django, flask, fastapi, spring boot, spring framework, .net, asp.net, laravel, symfony, rails, phoenix, gin, fiber, tensorflow, pytorch, keras, scikit-learn, xgboost, lightgbm, opencv, nltk, spacy, transformers, hugging face, langchain, machine learning, deep learning, natural language processing, computer vision, reinforcement learning, generative ai, large language models, prompt engineering, retrieval augmented generation, feature engineering, model evaluation, mlops, data science, statistics, probability, linear algebra, experimentation, a/b testing, causal inference, time series, forecasting, regression, classification, clustering, recommendation systems, anomaly detection, speech recognition, aws, azure, google cloud, oracle cloud, ibm cloud, amazon ec2, amazon s3, aws lambda, aws glue, aws emr, amazon redshift, dynamodb, azure functions, azure devops, google compute engine, bigquery, cloud run, cloud storage, firebase, docker, kubernetes, helm, terraform, pulumi, ansible, jenkins, github actions, gitlab ci, circleci, travis ci, argo cd, istio, prometheus, grafana, datadog, splunk, elastic stack, elk stack, linux, unix, windows server, macos, git, github, gitlab, bitbucket, mercurial, svn, jira, confluence, linear, notion, slack, postman, insomnia, selenium, playwright, cypress, jest, mocha, pytest, junit, testng, cucumber, load testing, performance testing, security testing, integration testing, unit testing, end to end testing, mysql, postgresql, mariadb, sqlite, oracle database, microsoft sql server, mongodb, redis, cassandra, elasticsearch, neo4j, graphql, snowflake, databricks, apache spark, apache kafka, apache flink, apache airflow, dbt, hadoop, hive, presto, trino, pandas, numpy, scipy, matplotlib, seaborn, tableau, power bi, looker, excel, google sheets, pivot tables, data visualization, data modeling, etl, elt, data warehouse, data lake, data governance, data quality, master data management, business intelligence, information architecture, ux research, user experience design, user interface design, interaction design, wireframing, prototyping, figma, sketch, adobe xd, adobe photoshop, adobe illustrator, indesign, responsive design, accessibility, web performance, search engine optimization, digital analytics, google analytics, content management systems, wordpress, shopify, salesforce, hubspot, sap, oracle erp, workday, servicenow, quickbooks, tableau prep, six sigma, lean, supply chain, inventory management, procurement, financial modeling, financial analysis, accounting, bookkeeping, payroll, budgeting, forecasting, risk management, compliance, audit, cybersecurity, information security, threat modeling, penetration testing, vulnerability management, identity access management, encryption, public key infrastructure, zero trust, incident response, disaster recovery, business continuity, privacy engineering, gdpr, soc 2, hipaa, iso 27001, agile, scrum, kanban, waterfall, product management, project management, program management, roadmap planning, requirements gathering, stakeholder management, change management, process improvement, technical writing, documentation, solution architecture, enterprise architecture, integration architecture
""")

SOFT = _items("""
communication, written communication, verbal communication, presentation, public speaking, active listening, storytelling, facilitation, negotiation, persuasion, influence, collaboration, teamwork, cross functional collaboration, relationship building, networking, customer service, client management, stakeholder engagement, empathy, emotional intelligence, cultural awareness, diversity and inclusion, conflict resolution, mediation, diplomacy, coaching, mentoring, people management, leadership, strategic thinking, critical thinking, analytical thinking, creative thinking, problem solving, decision making, sound judgment, attention to detail, organization, time management, prioritization, multitasking, adaptability, flexibility, resilience, initiative, ownership, accountability, dependability, reliability, integrity, ethics, professionalism, self motivation, curiosity, continuous learning, growth mindset, innovation, resourcefulness, patience, composure, stress management, work ethic, collaboration skills, listening skills, facilitation skills, influencing skills, consulting, executive presence, business acumen, commercial awareness, customer empathy, service orientation, results orientation, data literacy, digital literacy, remote collaboration, cross cultural communication, giving feedback, receiving feedback, performance management, delegation, team building, hiring, interviewing, talent development, succession planning, organizational awareness, strategic planning, vision setting, goal setting, decision analysis, risk awareness, incident management, crisis management, presentation skills, meeting management, agenda setting, documentation skills, research, qualitative research, quantitative research, hypothesis testing, synthesis, summarization, editing, proofreading, copywriting, technical communication, visual communication, brand awareness, storytelling skills, sales, prospecting, lead generation, account management, business development, partnership development, consultative selling, objection handling, closing, marketing, market research, competitive analysis, product positioning, campaign planning, content strategy, social media, community management, event planning, recruiting, sourcing, candidate experience, employee relations, labor relations, compensation, benefits administration, workforce planning, training, instructional design, facilitation training, classroom management, curriculum development, health and safety, compliance mindset, quality assurance, process thinking, operational excellence, continuous improvement, lean thinking, systems thinking, entrepreneurial mindset, change agility, ambiguity tolerance, cultural intelligence, inclusion, collaboration, humility, transparency, trust building, consensus building, accountability mindset, strategic communication, customer advocacy, negotiation skills, interpersonal skills, leadership development
""")

CANONICAL_SKILLS = sorted(set(TECHNICAL + SOFT))

# Canonical aliases commonly seen in resumes and job descriptions.
_ALIAS_GROUPS = {
    "python": ["py"], "javascript": ["js", "ecmascript"], "typescript": ["ts"],
    "c++": ["cpp"], "c#": ["csharp", ".net c#"], "sql": ["structured query language"],
    "rest api": ["rest", "restful", "rest apis"], "machine learning": ["ml"],
    "deep learning": ["dl"], "natural language processing": ["nlp"],
    "generative ai": ["genai", "generative artificial intelligence"],
    "large language models": ["llm", "llms"], "retrieval augmented generation": ["rag"],
    "scikit-learn": ["sklearn", "scikit learn"], "tensorflow": ["tf"],
    "pytorch": ["torch"], "hugging face": ["huggingface"], "amazon s3": ["s3"],
    "amazon ec2": ["ec2"], "amazon redshift": ["redshift"], "google cloud": ["gcp"],
    "microsoft sql server": ["mssql", "sql server"], "google analytics": ["ga4"],
    "power bi": ["powerbi"], "user experience design": ["ux", "user experience"],
    "user interface design": ["ui", "user interface"], "search engine optimization": ["seo"],
    "object oriented programming": ["oop"], "test driven development": ["tdd"],
    "a/b testing": ["ab testing", "a-b testing", "split testing"], "continuous integration": ["ci"],
    "continuous delivery": ["cd"], "amazon web services": ["aws"], "google sheets": ["sheets"],
    "human resources": ["hr"], "customer relationship management": ["crm"],
    "keynote": ["apple keynote"], "communication": ["communications"],
    "collaboration": ["collaborative"], "problem solving": ["problem-solving"],
    "decision making": ["decision-making"], "attention to detail": ["detail oriented", "detail-oriented"],
    "time management": ["time-management"], "critical thinking": ["critical-thinking"],
    "project management": ["project-management"], "product management": ["product-management"],
    "people management": ["team management"], "presentation": ["presentations"],
    "public speaking": ["speaking"], "active listening": ["active-listening"],
}

ALIAS_TO_CANONICAL = {skill: skill for skill in CANONICAL_SKILLS}
for canonical, aliases in _ALIAS_GROUPS.items():
    if canonical in ALIAS_TO_CANONICAL:
        for alias in aliases:
            ALIAS_TO_CANONICAL[alias.lower()] = canonical

# A defensive count check prevents accidental catalog shrinkage during maintenance.
assert len(CANONICAL_SKILLS) >= 300, f"skills catalog must contain at least 300 entries, got {len(CANONICAL_SKILLS)}"
