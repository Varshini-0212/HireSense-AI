# HireSense AI – Complete Installation and Execution Guide

This guide explains how to download, configure, install and run HireSense AI on a Windows computer.

It can be used both on the original development computer and on a different laptop.

## 1. System Requirements

Install the following software before running the project.

| Software         | Required version      |
| ---------------- | --------------------- |
| Operating System | Windows 10/11         |
| Java JDK         | 17                    |
| Maven            | 3.9 or later          |
| Python           | 3.10 or later         |
| Node.js          | LTS version           |
| npm              | Included with Node.js |
| MySQL Server     | 8.0 or later          |
| MySQL Workbench  | Recommended           |
| Git              | Latest version        |
| IntelliJ IDEA    | Recommended           |
| VS Code          | Optional              |

Verify installations using:

```powershell
java -version
mvn -version
python --version
node --version
npm --version
git --version
```

## 2. Download the Project from GitHub

Open PowerShell and execute:

```powershell
git clone https://github.com/Varshini-0212/HireSense-AI.git
```

Navigate to the project:

```powershell
cd HireSense-AI
```

The project contains three main components:

```text
HireSense-AI/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── frontend/
│   ├── src/
│   ├── package.json
│   └── Dockerfile
│
├── ml-service/
│   ├── app/
│   ├── requirements.txt
│   └── Dockerfile
│
├── docs/
├── docker-compose.yml
├── .env.example
├── .gitignore
└── README.md
```

## 3. Configure MySQL Database

1. Install MySQL Server 8.0 or later.
2. Open MySQL Workbench.
3. Connect to your local MySQL server.
4. Open a new SQL query tab.
5. Execute:

```sql
CREATE DATABASE hiresense
CHARACTER SET utf8mb4
COLLATE utf8mb4_0900_ai_ci;

CREATE USER 'hiresense'@'localhost'
IDENTIFIED BY 'YOUR_SECURE_PASSWORD';

GRANT ALL PRIVILEGES ON hiresense.*
TO 'hiresense'@'localhost';

FLUSH PRIVILEGES;
```

Replace `YOUR_SECURE_PASSWORD` with a password of your choice.

The Spring Boot backend initializes the database schema and seed data through its SQL initialization scripts.

## 4. Run the Python ML Service

Open the first PowerShell terminal.

Navigate to the ML service:

```powershell
cd HireSense-AI\ml-service
```

Create a Python virtual environment:

```powershell
py -3.12 -m venv .venv
```

Activate it:

```powershell
.venv\Scripts\activate
```

Upgrade pip:

```powershell
python -m pip install --upgrade pip
```

Install dependencies:

```powershell
pip install -r requirements.txt
```

Start the ML service:

```powershell
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

Verify:

* API documentation: http://localhost:8000/docs
* Health check: http://localhost:8000/health

Keep this terminal running.

## 5. Run the Spring Boot Backend

Open a second PowerShell terminal.

Navigate to the backend:

```powershell
cd HireSense-AI\backend
```

Configure Java 17:

```powershell
$env:JAVA_HOME = "PATH_TO_YOUR_JDK_17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
```

Set the database configuration:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/hiresense?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"

$env:DB_USERNAME = "hiresense"

$env:DB_PASSWORD = "YOUR_SECURE_PASSWORD"
```

Set the JWT secret:

```powershell
$env:JWT_SECRET = "REPLACE_WITH_A_RANDOM_SECRET_OF_AT_LEAST_32_CHARACTERS"
```

Configure the ML service:

```powershell
$env:ML_SERVICE_URL = "http://localhost:8000"
```

Start the backend:

```powershell
mvn spring-boot:run
```

If Maven is not installed globally, use the Maven tool window in IntelliJ IDEA or install Maven and configure its PATH.

Wait until the terminal displays:

```text
Tomcat started on port 8080
```

Verify:

* Backend: http://localhost:8080
* Swagger documentation: http://localhost:8080/swagger-ui.html

Keep this terminal running.

## 6. Run the React Frontend

Open a third PowerShell terminal.

Navigate to the frontend:

```powershell
cd HireSense-AI\frontend
```

Install dependencies:

```powershell
npm install
```

Configure the backend URL:

```powershell
$env:VITE_API_BASE_URL = "http://localhost:8080"
```

Start the frontend:

```powershell
npm run dev
```

Open:

http://localhost:5173

The HireSense AI application should now be accessible through your browser.

## 7. Application URLs

| Component             | URL                                   |
| --------------------- | ------------------------------------- |
| React Frontend        | http://localhost:5173                 |
| Spring Boot Backend   | http://localhost:8080                 |
| Swagger UI            | http://localhost:8080/swagger-ui.html |
| Python ML Service     | http://localhost:8000                 |
| FastAPI Documentation | http://localhost:8000/docs            |
| ML Health Check       | http://localhost:8000/health          |

## 8. Running the Project on Another Computer

When setting up HireSense AI on another laptop:

1. Install all software listed in System Requirements.
2. Clone the GitHub repository.
3. Create the MySQL database and database user.
4. Configure Java 17.
5. Create a new Python virtual environment.
6. Install Python dependencies.
7. Install frontend dependencies using npm.
8. Configure the environment variables.
9. Start the ML service.
10. Start the Spring Boot backend.
11. Start the React frontend.

**Important:** Virtual environments, installed dependencies, database credentials and local runtime files are not transferred through GitHub. They must be configured on each computer.

Do not copy passwords or JWT secrets from another computer. Generate your own local credentials.

## 9. Restarting the Application

Whenever the application is closed or the computer is restarted, start the three services again.

Use three separate PowerShell terminals.

**Terminal 1 – ML Service**

```powershell
cd HireSense-AI\ml-service
.venv\Scripts\activate
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

**Terminal 2 – Backend**

```powershell
cd HireSense-AI\backend

$env:JAVA_HOME = "PATH_TO_YOUR_JDK_17"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$env:DB_URL = "jdbc:mysql://localhost:3306/hiresense?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "hiresense"
$env:DB_PASSWORD = "YOUR_SECURE_PASSWORD"
$env:JWT_SECRET = "YOUR_RANDOM_JWT_SECRET"
$env:ML_SERVICE_URL = "http://localhost:8000"

mvn spring-boot:run
```

**Terminal 3 – Frontend**

```powershell
cd HireSense-AI\frontend
$env:VITE_API_BASE_URL = "http://localhost:8080"
npm run dev
```

## 10. Troubleshooting

| Problem                  | Possible solution                                             |
| ------------------------ | ------------------------------------------------------------- |
| Java not recognized      | Install JDK 17 and configure JAVA_HOME                        |
| Maven not recognized     | Install Maven and add its bin directory to PATH               |
| MySQL connection failed  | Check MySQL service, database name, username and password     |
| Port 8080 already in use | Stop the existing backend process                             |
| Port 8000 already in use | Stop the existing ML service                                  |
| Port 5173 already in use | Stop the existing frontend process                            |
| Python module not found  | Activate `.venv` and install requirements                     |
| npm command not found    | Install Node.js and reopen PowerShell                         |
| API unavailable          | Ensure the backend is running and frontend API URL is correct |
| Resume upload failed     | Check file type, file size and backend storage configuration  |

## 11. Stopping the Application

Press:

```text
Ctrl + C
```

in each of the three service terminals.

This stops the running application services.

The MySQL database remains available independently.

---

**Note:** This setup is intended for local development. For public production deployment, configure cloud database credentials, persistent resume storage, HTTPS, secure JWT secrets, production CORS and appropriate access controls.
