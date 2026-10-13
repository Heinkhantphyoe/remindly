# Remindly

Remindly is a production-style event reminder service built incrementally with a React frontend and Spring Boot backend.

## Current progress: Step 10 — final verification and deployment preparation

The project foundation, PostgreSQL schema, authentication, user profile endpoints, reminder CRUD API, notification scheduler, notification API, frontend reminder experience, and notification UI are in place. Production build checks and local deployment documentation are complete.

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

Reminder endpoints require a JWT access token:

```text
POST   /api/reminders
GET    /api/reminders
GET    /api/reminders/{id}
PUT    /api/reminders/{id}
PATCH  /api/reminders/{id}/completion
DELETE /api/reminders/{id}
```

The list endpoint supports `completed`, `category`, `dueFrom`, `dueTo`, `page`, `size`, and `sort` query parameters. For example:

```text
GET /api/reminders?completed=false&sort=dueAt,asc&page=0&size=20
```

The notification scheduler runs once per minute by default. It creates due-time and optional advance notifications, skips completed reminders, recovers missed notifications from the previous 24 hours, and relies on the database uniqueness constraint to prevent duplicates. The interval and recovery window can be configured with `NOTIFICATION_SCHEDULER_FIXED_RATE_MS` and `NOTIFICATION_RECOVERY_WINDOW_HOURS`.

Notification endpoints require a JWT access token:

```text
GET   /api/notifications
GET   /api/notifications/unread-count
PATCH /api/notifications/{id}/read
PATCH /api/notifications/read-all
```

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

## Verification

Run the backend checks:

```powershell
cd backend
mvn test
mvn -DskipTests package
```

Run the frontend checks:

```powershell
cd frontend
npm install
npm run build
```

Validate the local Compose configuration from the repository root:

```powershell
docker compose config
```

For deployment, set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `JWT_SECRET` through environment variables instead of using local development defaults.

