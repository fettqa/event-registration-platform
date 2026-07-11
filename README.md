# Event Registration

Pet project: API, automated tests, k6 performance.

## Structure
- `app/` — Spring Boot API (Java 21)
- `tests-api/` — Python REST tests (planned)
- `tests-e2e/` — Playwright E2E (planned)
- `perf/k6/` — k6 load tests (planned)

## Run app
```bash
cd app
./gradlew bootRun
```
Swagger: http://localhost:8080/swagger-ui.html  
Health: http://localhost:8080/actuator/health

## API
- `POST /api/events/{id}` — create an event
- `POST /api/events/{id}/registrations` — register for an event
- Returns 409 when seats are full or email already registered

## Run with PostgreSQL (Docker)

```bash
# 1. Start database
docker compose up -d

# 2. Run app with docker profile
cd app
./gradlew bootRun --args='--spring.profiles.active=docker'
```

Stop database:
```bash
docker compose down
```

## Run tests (H2, no Docker required)
```bash
cd app
./gradlew test
```