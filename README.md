![App CI](https://github.com/fettqa/event-registration-platform/actions/workflows/app-ci.yml/badge.svg)

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
- `POST /api/events` — create an event
- `POST /api/events/{id}/registrations` — register for an event
- Returns 409 when seats are full or email already registered

## CI
Tests run automatically on push/PR via GitHub Actions.

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

## Performance (k6)

### Prerequisites
1. Start the app (`cd app && ./gradlew bootRun`)
2. Install [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/)

### Smoke
```bash
k6 run perf/k6/smoke.js
```

### Load (registrations)
```bash
k6 run perf/k6/load-register.js
```

### Spike
```bash
k6 run perf/k6/spike.js
```

### Results (local)

| Test  | VUs / stages | Duration | p95 | Failed | Checks |
|-------|-------------|-------|-----|--------|--------|
| Smoke | 2 VU        | 30s   | 12.76 ms | 0 %    | 100 %  |
| Load  | 0→50→0      | ~3m   | 52.2ms | 0 %    | 100 %  |
| Spike | 10→100→0    | ~1m   | 105.13 ms | 0 %    | 100 %  |

## Python API tests

```bash
# 1. Start the app
cd app && ./gradlew bootRun

# 2. In another terminal
cd tests-api
python -m venv .venv
# Windows:
.venv\Scripts\activate
pip install -r requirements.txt
pytest
```