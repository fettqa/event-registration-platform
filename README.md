![App CI](https://github.com/fettqa/event-registration-platform/actions/workflows/app-ci.yml/badge.svg)
![Python API Tests](https://github.com/fettqa/event-registration-platform/actions/workflows/python-api-tests.yml/badge.svg)
![E2E Java](https://github.com/fettqa/event-registration-platform/actions/workflows/e2e-java.yml/badge.svg)
![E2E Python](https://github.com/fettqa/event-registration-platform/actions/workflows/e2e-python.yml/badge.svg)

## Event Registration Platform

## About
Event Registration API — pet project.
Covers:
- REST API (Spring Boot, Java 21, Flyway)
- Automated API tests in **Java (REST Assured)** and **Python (pytest + httpx)**
- E2E UI tests with **Playwright (Java + Python)**
- CI on **GitHub Actions** (Java tests, Python API tests, Playwright E2E)
- Performance tests with **k6** (smoke / load / spike)
- Dockerized **PostgreSQL** for local prod-like runs

## Web UI (Thymeleaf)

Open after `bootRun`: http://localhost:8080/

| Page | URL | Features |
|------|-----|----------|
| Events list | `/` | search by name, pagination (100/page) |
| Create event | `/events/new` | validation errors, redirect to created event |
| Event details | `/events/{id}` | seats left, register form, registrations list with search + pagination |

UI uses the same services as REST API (SSR forms, not JSON).

## Auth
Default admin (seeded on startup):
email:    admin@example.com
password: admin123

POST /api/auth/register  → USER + accessToken
POST /api/auth/login     → accessToken

Header: Authorization: Bearer <accessToken>

Swagger: Authorize → bearerAuth → insert token

## Domain rules

- Unique event name
- Unique email per event
- Cannot register when seats are full (409)
- Concurrent registration protected (row lock / race handling)
- Remaining seats shown on event details page

## Tech stack
| Area | Tools                                                   |
|------|---------------------------------------------------------|
| Backend | Java 21, Spring Boot, JPA, Flyway, H2 / PostgreSQL, JWT |
| API docs | springdoc OpenAPI (Swagger UI)                          |
| Java tests | JUnit 5, REST Assured, MockMvc                          |
| Python API tests | pytest, httpx                                           |
| E2E | Playwright (Java + Python)                              |
| Performance | k6                                                      |
| CI/CD | GitHub Actions                                          |
| Infra | Docker Compose                                          |

## Structure
- `app/` — Spring Boot API (Java 21) + Thymeleaf UI
- `tests-api/` — Python REST tests (pytest + httpx)
- `tests-e2e/java/` — Playwright E2E (Java)
- `tests-e2e/python/` — Playwright E2E (Python)
- `perf/k6/` — k6 load tests (smoke / load / spike)

## Quick start
```bash
# App (H2)
cd app && ./gradlew bootRun
# Web UI
open http://localhost:8080/
# Java tests
cd app && ./gradlew test
# Python API tests (app must be running)
cd tests-api && pytest
# k6 smoke
k6 run perf/k6/smoke.js
# Playwright E2E (app must be running)
cd tests-e2e/java && ./gradlew installPlaywright && ./gradlew test
# Playwright E2E Python (app must be running)
cd tests-e2e/python
# activate venv (py .\.venv\Scripts\activate), then:
pytest

Swagger: http://localhost:8080/swagger-ui.html  
Health: http://localhost:8080/actuator/health
```

## API (REST)
| Method | Path | Notes | Access |
|--------|------|-------|--------|
| POST | `/api/events` | create | Admin |
| GET | `/api/events` | list / filter | Public |
| GET | `/api/events/{id}` | by id | Public |
| PATCH | `/api/events/{id}` | update | Admin |
| DELETE | `/api/events/{id}` | delete | Admin |
| POST | `/api/events/{id}/registrations` | register (201 / 409) | Public |
Swagger: http://localhost:8080/swagger-ui.html

## CI
Tests run automatically on push/PR via GitHub Actions.
## CI workflows

| Workflow | Runs |
|----------|------|
| App CI | `./gradlew test` |
| Python API Tests | bootJar → start app → pytest |
| E2E Java | Playwright Java |
| E2E Python | Playwright Python |

Badges at the top show current status.

## Run with PostgreSQL (Docker)

```bash
# 1. Start database
cd app
docker compose up -d

# 2. Run app with docker profile
cd app
./gradlew bootRun --args='--spring.profiles.active=docker'
```

Stop database:
```bash
docker compose down
```

Clean database (drop + recreate):
```bash
docker compose down -v
docker compose up -d
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

| Test  | VUs / stages | Duration | p95      | Failed | Checks |
|-------|-------------|-------|----------|--------|--------|
| Smoke | 2 VU        | 30s   | 43.59ms  | 0 %    | 100 %  |
| Load  | 0→50→0      | ~3m   | 222.16ms | 0 %    | 100 %  |
| Spike | 10→100→0    | ~1m   | 664.22ms | 0 %    | 100 %  |

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

## Playwright E2E (Java)

App must be running on `:8080`.

```bash
# 1. Start the app
cd app && ./gradlew bootRun

# 2. Install browser (once)
cd tests-e2e/java
./gradlew installPlaywright

# 3. Run E2E
./gradlew test
# optional:
./gradlew test -DbaseUrl=http://localhost:8080
```

## Playwright E2E (Python)

Use **Python 3.12** (3.14 may fail installing `greenlet` wheels on Windows).

```bash
# 1. Start app
cd app && ./gradlew bootRun

# 2. Setup (first time)
cd tests-e2e/python
py -3.12 -m venv .venv
# Windows:
.venv\Scripts\activate
pip install -r requirements.txt
playwright install chromium

# 3. Run
pytest
# headed:
pytest --headed
```

![Events](Events.png)
![New event](New_event.png)
![Event details](Event_details.png)
