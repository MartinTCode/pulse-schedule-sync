# pulse-schedule-sync

pulse-schedule-sync is a proof-of-concept system integration tool for synchronizing finalized schedules from an external scheduling system into a calendar platform.

The application retrieves schedule data, allows a responsible user to review and adjust event details (such as location and description), and then publishes the events to a target calendar via API.

## Key Features

- Fetches schedule data from an external scheduling source

- User review and manual adjustment step before publishing

- Automated creation of calendar events via REST API

- Server-side integration layer acting as an API proxy

- Desktop UI for previewing and editing events

## Architecture Overview

- JavaFX desktop client for user interaction

- Embedded REST server (JAX-RS / Jersey) for integration logic

- Service-oriented design with clear separation between UI, domain, and integrations

- Optional persistence layer for drafts, mappings, or transfer history

## Tech Stack

- Java 21

- JavaFX

- JAX-RS (Jersey) with embedded HTTP server

- PostgreSQL (optional)

- JPA / Hibernate

- Flyway

- Maven

## Getting Started

This section describes how to run the application locally for development and testing.

### Prerequisites

- Java 21

- Maven 3.9+

- Git

- Access to Canvas (for API token)

Verify:

```bash
java -version
mvn -version
```
### 1. Clone repo
clone or download source code. 

### 2. Create local .env file and get token (required)
he application requires a Canvas API token to communicate with the Canvas API.
For local development, this token is provided via a .env file located in the project root directory.

The .env file:

- is not committed to the repository

- is distributed privately to authorized users

- must be loaded into the environment before running the application

#### 2.1 Create .env in the project root
Create a file named only ".env" in the root of the project (same directory as pom.xml). Should look like below when done:
```bash
[---]schedule-sync/
├── .env
├── .gitignore
├── pom.xml
├── README.md
└── src/
```
#### 2.2.1 Generate a Canvas Personal Access Token
1. Log in to Canvas

2. Go to Account → Settings

3. Scroll to Approved Integrations

4. Click + New Access Token

5. Give it a purpose (e.g. TimeEdit Transfer PoC)

6. Copy the generated token

7. Paste it into .env as CANVAS_TOKEN (see 2.2.2 below)

#### 2.2.2 Add Canvas configuration
Open .env and add the following variables:
```bash
CANVAS_BASE_URL=https://canvas.<your-organization-domain>
CANVAS_TOKEN=<your-canvas-personal-access-token>
```
#### 2.3 Load .env into the environment
The application reads configuration from environment variables.
The .env file must therefore be loaded before running Maven.

The application reads secrets from environment variables:
```bash
String token = System.getenv("CANVAS_TOKEN");
```
If the token is missing or empty, the application will fail fast with a clear error message.

Open terminal in source directory where the .env file is located along with the root folder of the source code and run:

##### Linux / macOS
```bash
export $(cat .env | xargs)
```

##### Windows (PowerShell)
```bash
Get-Content .env | ForEach-Object {
  if ($_ -match "=") {
    $name, $value = $_ -split "=", 2
    setx $name $value
  }
}
```
#### 2.4 Security note

Never commit .env to the repository

Never share the file publicly

Treat the Canvas token as a password

Rotate the token if it is accidentally exposed

#### 2.5 Verification (optional)
To verify that the token is available to the application, you may run:

```bash
echo $CANVAS_TOKEN
```

If the variable is set correctly, the value should be printed (do not share it).

#### 2.6 Verify Canvas acess
Only works if step 2.3 exectued successfully. Run the following commands in the terminal to test 

##### Linux / macOS
```bash
curl -H "Authorization: Bearer $CANVAS_TOKEN" \
     $CANVAS_BASE_URL/api/v1/users/self/profile
```
##### Windows 11/10 (powershell)
```bash
curl -H "Authorization: Bearer $env:CANVAS_TOKEN" `
     "$env:CANVAS_BASE_URL/api/v1/users/self/profile"
```
### 3. Run the applications.
Can we run with frontend and rest backend together via:
```bash
mvn javafx:run
```
or to just run backend:
```bash
mvn exec:java
```

#### 3.1 Running the Test Dashboard (API Testing UI)
The test dashboard provides a visual interface to test API endpoints and verify Canvas API connectivity.

**Run test dashboard:**
```bash
mvn javafx:run -Dtest-dashboard=true
```

The test dashboard displays:
- Backend health status (Health Check Endpoint)
- Canvas API connectivity test results (Canvas API Test Endpoint)
- Expected response formats for each endpoint

This is useful for:
- Verifying Canvas credentials are loaded correctly
- Debugging integration issues
- Confirming REST server is running

**Normal UI (Schedule Overview):**
```bash
mvn javafx:run
```

### 4. Test endpoints.
once the application is running, we can test it using Postman or just the a web browser (URL):

##### Health check

```bash
GET http://localhost:8080/health
```

Expected response:
```nginx
ok
```

##### Schedule endpoint (TimeEdit)
```bash
GET http://localhost:8080/api/schedule
```

##### Transfer endpoint (Canvas)
```bash
POST http://localhost:8080/api/transfer
```

## Scope

This project is intended as a technical demonstration of:

- System integration

- Service-oriented architecture principles

- API orchestration

- Clean layering between UI, services, and external systems

It is not a production-ready system, but a focused and extensible integration prototype.
