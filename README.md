# Remindly

Remindly is a production-style event reminder service built incrementally with a React frontend and Spring Boot backend.

## Current progress: Step 2 — database foundation

The project foundation and initial PostgreSQL schema are in place. Authentication, reminder endpoints, and scheduling will be added in later steps.

## Requirements

- Java 21
- Maven 3.9+
- Node.js 22+
- npm 10+
- PostgreSQL 15+ (local PostgreSQL on port 5432 is supported)
- Docker Desktop (optional, for the Compose PostgreSQL service)

## Structure

```text
backend/   Spring Boot API
frontend/  React + Vite client
```

## Run the backend

```powershell
cd backend
mvn spring-boot:run
```

The backend listens on `http://localhost:8080`.

Health check: `http://localhost:8080/actuator/health`

## Run the frontend

```powershell
cd frontend
npm install
npm run dev
```

The frontend listens on the URL printed by Vite, normally `http://localhost:5173`.

## PostgreSQL

The project can use the existing PostgreSQL installation on port `5432`. Create a database and user matching `.env.example`, or start the containerized option:

```powershell
docker compose up -d postgres
```

Flyway applies database migrations automatically when the backend starts. The first migration creates the users, refresh tokens, reminders, and notifications tables.

If using the existing PostgreSQL installation, create the configured database and user before starting the backend. Alternatively, start the Compose database service with the command above.

