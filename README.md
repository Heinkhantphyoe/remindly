# Remindly

Remindly is a production-style event reminder service built incrementally with a React frontend and Spring Boot backend.

## Step 1: project foundation

This step establishes the monorepo structure and verifies the application shells. It does not include database tables, authentication, or reminder features yet.

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

Database schema migrations will be added in Step 2.

