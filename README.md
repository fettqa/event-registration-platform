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

Swagger: http://localhost:8080/swagger-ui.html  
Health: http://localhost:8080/actuator/health