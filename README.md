![App CI](https://github.com/fettqa/event-registration-platform/actions/workflows/app-ci.yml/badge.svg)
![App Unit Tests](https://github.com/fettqa/event-registration-platform/actions/workflows/app-unit-tests.yml/badge.svg)
![API RestAssured Java](https://github.com/fettqa/event-registration-platform/actions/workflows/api-restassured-java.yml/badge.svg)
![API httpx Python](https://github.com/fettqa/event-registration-platform/actions/workflows/api-httpx-python.yml/badge.svg)
![Web Playwright Java](https://github.com/fettqa/event-registration-platform/actions/workflows/web-playwright-java.yml/badge.svg)
![Web Playwright Python](https://github.com/fettqa/event-registration-platform/actions/workflows/web-playwright-python.yml/badge.svg)
![Mobile Appium Kotlin](https://github.com/fettqa/event-registration-platform/actions/workflows/mobile-appium-kotlin.yml/badge.svg)
![Mobile Appium Python](https://github.com/fettqa/event-registration-platform/actions/workflows/mobile-appium-python.yml/badge.svg)
![Mobile Maestro](https://github.com/fettqa/event-registration-platform/actions/workflows/mobile-maestro.yml/badge.svg)
![k6 Performance](https://github.com/fettqa/event-registration-platform/actions/workflows/k6-performance.yml/badge.svg)

## Event Registration Platform

Demo: [https://event-registration-jesq.onrender.com/](https://event-registration-jesq.onrender.com/)

## About

Event Registration API — pet project.
Covers:

- REST API (Spring Boot, Java 21, Flyway)
- Automated API tests in **Java (REST Assured)** and **Python (pytest + httpx)**
- E2E UI tests with **Playwright (Java + Python)**
- CI on **GitHub Actions** (Java tests, Python API tests, Playwright E2E)
- Performance tests with **k6** (smoke / load / spike)
- Dockerized **PostgreSQL** for local prod-like runs
- Deploy on **Render** (Docker + free Postgres; see README Deploy section)



## Web UI (Thymeleaf)

Open after `bootRun`: [http://localhost:8080/](http://localhost:8080/)


| Page          | URL            | Features                                                                  |
| ------------- | -------------- | ------------------------------------------------------------------------- |
| Events list   | `/`            | search by name, pagination (100/page)                                     |
| Login         | `/login`       | JWT via `/api/auth/login`, token in `localStorage`                        |
| Register      | `/register`    | full name + email + password → USER + JWT                                 |
| Admin Panel   | `/adminPanel`  | ADMIN only; Users tab to set USER / SUPER_USER                            |
| Create event  | `/events/new`  | ADMIN / SUPER_USER only; calls `POST /api/events`                         |
| Event details | `/events/{id}` | guests browse; logged-in users register from profile (no name/email form) |


Create event uses the secured REST API from the browser (`Authorization: Bearer ...`). Event registration requires login; name/email come from the user account.

## Auth

Default admin (seeded on startup):
email:    [admin@example.com](mailto:admin@example.com)
password: admin123

Roles:

- `ADMIN` — seeded account (and any admins added via `AdminUserInitializer` / DB)
- `SUPER_USER` — can create events and register for events
- `USER` — can register for events only

POST /api/auth/register  → USER + accessToken (requires fullName)
POST /api/auth/login     → accessToken

Header: Authorization: Bearer 

Swagger: Authorize → bearerAuth → insert token

## Domain rules

- Unique event name
- Unique email per event
- Cannot register when seats are full (409)
- Concurrent registration protected (row lock / race handling)
- Remaining seats shown on event details page
- Successful registration can emit Kafka event `registration.created` (profile `kafka`); with `mail` → confirmation email via Mailpit SMTP


## Tech stack


| Area             | Tools                                                                 |
| ---------------- |-----------------------------------------------------------------------|
| Backend          | Java 21, Spring Boot, JPA, Flyway, H2 / PostgreSQL, JWT, Spring Kafka |
| API docs         | springdoc OpenAPI (Swagger UI)                                        |
| Java tests       | JUnit 5, REST Assured, MockMvc, Allure, EmbeddedKafka                 |
| Python API tests | pytest, httpx, Allure, kafka-python                                   |
| E2E              | Playwright (Java + Python), Allure                                    |
| Performance      | k6 (+ HTML/JSON summary)                                              |
| CI/CD            | GitHub Actions                                                        |
| Deploy           | Render (Docker Blueprint)                                             |
| Infra            | Docker Compose (Postgres + Kafka)                                     |




## Structure

- `app/` — Spring Boot API (Java 21) + Thymeleaf UI
- `tests-api/python/` — Python REST tests (pytest + httpx)
- `tests-api/java/` — Java REST tests (RestAssured black-box)
- `tests-web/java/` — Playwright E2E (Java; desktop + `mobileChromeTest`)
- `tests-web/python/` — Playwright E2E (Python; desktop + `mobile_chrome/` smoke)
- `tests-perf/k6/` — k6 load tests (smoke / load / spike)
- `android/` — Android client (Kotlin; см. [`android/README.md`](android/README.md))
- `tests-mobile/` — mobile UI E2E (Maestro / Appium Kotlin / Appium Python; см. [`tests-mobile/README.md`](tests-mobile/README.md))



## Quick start

```bash
# App (H2)
cd app && ./gradlew bootRun
# Web UI
open http://localhost:8080/
# Java tests
cd app && ./gradlew test
# App (H2 + Kafka)
./gradlew bootRun --args='--spring.profiles.active=kafka'
# Kafka + Mailpit (mock inbox UI http://localhost:8025)
#   docker compose up -d kafka mailpit
#   ./gradlew bootRun --args='--spring.profiles.active=kafka,mail'
# or Postgres + Kafka
./gradlew bootRun --args='--spring.profiles.active=docker,kafka'
# Python API tests (app must be running)
cd tests-api/python/python && pytest
# Java API tests (RestAssured black-box)
cd tests-api/python/java && ./gradlew test
# k6 smoke
k6 run tests-perf/k6/smoke.js
# Playwright E2E (app must be running)
cd tests-web/java && ./gradlew installPlaywright && ./gradlew test
# Mobile Chrome smoke: ./gradlew mobileChromeTest
# Playwright E2E Python (app must be running)
cd tests-web/python
# activate venv (.\.venv\Scripts\activate), then:
pytest
# Mobile Chrome smoke (web):
pytest mobile_chrome -m smoke

# Android (emulator): bootRun + adb reverse, then open android/ in Android Studio
#   adb reverse tcp:8080 tcp:8080
#   cd android && ./gradlew assembleDebug
# Details / Render BASE_URL: android/README.md
# Mobile UI E2E (Maestro / Appium): tests-mobile/README.md
#   emulator (-list-avds / -avd Pixel_8) → bootRun → adb reverse → APK → Appium → pytest / gradlew test
# CI: app-ci.yml → android-app-ci → mobile-*.yml (or Run workflow on a suite)

Swagger: http://localhost:8080/swagger-ui.html  
Health: http://localhost:8080/actuator/health
```

## Android

Клиент в [`android/`](android/README.md): список Events для гостя, login/register, create event, registrations, delete по ролям, Admin Panel, поиск.

| Стенд | `BASE_URL` в `android/app/build.gradle.kts` |
|-------|-----------------------------------------------|
| Local + emulator | `http://127.0.0.1:8080/` + `adb reverse tcp:8080 tcp:8080` |
| Render | `https://event-registration-jesq.onrender.com/` (без reverse) |

Mobile QA (Maestro / Appium): [`tests-mobile/README.md`](tests-mobile/README.md).

## API (REST)


| Method                                                                                  | Path                                              | Notes                             | Access                                           |
| --------------------------------------------------------------------------------------- | ------------------------------------------------- | --------------------------------- | ------------------------------------------------ |
| POST                                                                                    | `/api/events`                                     | create                            | Admin                                            |
| GET                                                                                     | `/api/events`                                     | list all (array); with `?page=&size=&q=` → paged search | Public                                           |
| GET                                                                                     | `/api/events/{id}`                                | by id                             | Public                                           |
| PATCH                                                                                   | `/api/events/{id}`                                | update                            | Admin                                            |
| DELETE                                                                                  | `/api/events/{id}`                                | delete                            | Admin                                            |
| POST                                                                                    | `/api/events/{id}/registrations`                  | register current user (201 / 409) | Authenticated                                    |
| GET                                                                                     | `/api/events/{id}/registrations`                  | list; with `?page=&size=&q=` → paged | Public                                        |
| DELETE                                                                                  | `/api/events/{id}/registrations/{registrationId}` | delete registration               | Admin: any; Super User: on own events; User: own |
| DELETE                                                                                  | `/api/events/{id}`                                | delete event                      | Admin: any; Super User: own events               |
| Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html) |                                                   |                                   |                                                  |




## CI workflows

Push/PR entry point is **App CI** (`app-ci.yml`): path filters decide what runs, builds `bootJar` once, then calls suites via `workflow_call` (shared JAR; Android APK via child `android-app-ci.yml`).

| Workflow | File | Role |
|----------|------|------|
| App CI | `app-ci.yml` | Orchestrator (push/PR): detect changes → JAR → selective suites |
| App Unit Tests | `app-unit-tests.yml` | Called when `app/**` changes; also manual dispatch |
| Android App CI | `android-app-ci.yml` | Child of App CI: Android unit + APK + mobile fan-out |
| API RestAssured Java | `api-restassured-java.yml` | `workflow_call` / manual (`bootJar` on dispatch) |
| API httpx Python | `api-httpx-python.yml` | same |
| Web Playwright Java | `web-playwright-java.yml` | same |
| Web Playwright Python | `web-playwright-python.yml` | same |
| k6 Performance | `k6-performance.yml` | same (+ scenario input) |
| Mobile Maestro / Appium | `mobile-*.yml` | call: download JAR+APK; dispatch: build both |

Manual run of a single suite: **Actions** → suite workflow → **Run workflow** (builds artifacts locally).

## Deploy (Render)

Free tier: web service **sleeps after ~15 min** without traffic (first request ~1 min cold start).  
Free Postgres **expires after 30 days** (then upgrade or recreate).

### Option A — Blueprint (recommended)

1. Push these files to `main`: `Dockerfile`, `render.yaml`, `application-render.yml`
2. Open [Render Dashboard](https://dashboard.render.com) → **New** → **Blueprint**
3. Connect `fettqa/event-registration-platform`, apply `render.yaml`
4. Wait for build; open the service URL (HTTPS)

Default admin (seeded): `admin@example.com` / `admin123`

### Option B — Manual

1. Create **PostgreSQL** (Free) on Render → note host/port/db/user/password
2. Create **Web Service** → Docker, repo root, Dockerfile
3. Environment:
  - `SPRING_PROFILES_ACTIVE=render`
  - `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
  - `APP_JWT_SECRET` = long random string (≥32 chars)
4. Health check path: `/actuator/health`



### Local Docker image check

```bash
docker build -t event-registration .
# needs Postgres env vars + profile render, or run with H2 locally via default profile instead
```



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

## Kafka + Mailpit (registration emails)

Mailpit is a **local mock SMTP + inbox UI** (letters do not go to Gmail). After a successful registration with profiles `kafka,mail`, open the UI and read the message.

```bash
cd app
docker compose up -d kafka mailpit

./gradlew bootRun --args='--spring.profiles.active=kafka,mail'
```

1. Register for an event (UI or API) as a user.  
2. Open [http://localhost:8025](http://localhost:8025) — email `to` = registrant address.  
3. SMTP for the app: `localhost:1025` (see `application-mail.yml`).

Without profile `mail`, Kafka still works; the listener only logs.

**Tests:** Java `RegistrationMailKafkaApiTest` (GreenMail, in `./gradlew test`); Python API `@pytest.mark.mail` + E2E Playwright (Mailpit). Locally for Python/E2E mail tests use `kafka,mail` + Mailpit as above.



## Run tests (H2, no Docker required)

```bash
cd app
./gradlew test
```



## Performance (k6)



### Prerequisites

1. Start the app (`cd app && ./gradlew bootRun`)
2. Install [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/)

Run from the **repo root** (paths in `handleSummary` are relative to cwd):

### Smoke

```bash
k6 run tests-perf/k6/smoke.js
```



### Load (registrations)

```bash
k6 run tests-perf/k6/load-register.js
```



### Spike

```bash
k6 run tests-perf/k6/spike.js
```

After a run, open (files are named per scenario and do not overwrite each other):

- `tests-perf/k6/results/smoke-report.html` / `smoke-summary.json`
- `tests-perf/k6/results/load-report.html` / `load-summary.json`
- `tests-perf/k6/results/spike-report.html` / `spike-summary.json`



### Results (local)


| Test  | VUs / stages | Duration | p95      | Failed | Checks |
| ----- | ------------ | -------- | -------- | ------ | ------ |
| Smoke | 2 VU         | 30s      | 43.59ms  | 0 %    | 100 %  |
| Load  | 0→50→0       | ~3m      | 222.16ms | 0 %    | 100 %  |
| Spike | 10→100→0     | ~1m      | 664.22ms | 0 %    | 100 %  |




## Allure reports

Install the [Allure CLI](https://allurereport.org/docs/install/), run tests, then serve results:

```bash
# Java API (from app/)
./gradlew test
allure serve build/allure-results

# Java E2E (from tests-web/java/)
./gradlew test
allure serve build/allure-results

# Python API (from tests-api/)
pytest
allure serve allure-results

# Python E2E (from tests-web/python/)
pytest
allure serve allure-results
```



### GitHub Actions artifacts + GitHub Pages

| Suite | Pages path |
|-------|------------|
| App Unit Tests | `…/allure/app-unit-tests/<run_number>/` |
| API httpx Python | `…/allure/api-httpx-python/<run_number>/` |
| Web Playwright Java | `…/allure/web-playwright-java/<run_number>/` |
| Web Playwright Python | `…/allure/web-playwright-python/<run_number>/` |
| k6 | `…/k6/<scenario>/<run_number>/<scenario>-report.html` |

Example: `https://<owner>.github.io/<repo>/allure/app-unit-tests/42/`

PR builds from the **same repo** also publish to Pages. Fork PRs keep artifacts only (token limits).

**One-time setup:** Repo → Settings → Pages → Source = Deploy from branch → `gh-pages`.

k6 **smoke** runs on push/PR when `app/` or `tests-perf/k6/**` change.  
Load/spike: Actions → **k6 Performance** → Run workflow → choose scenario.

## Python API tests

```bash
# 1. Start the app
cd app && ./gradlew bootRun

# 2. In another terminal
cd tests-api/python
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
cd tests-web/java
./gradlew installPlaywright

# 3. Desktop E2E
./gradlew test
# optional:
./gradlew test -DbaseUrl=http://localhost:8080

# Mobile Chrome smoke (Pixel 7 device profile — web DOM, not Appium)
./gradlew mobileChromeTest
```



## Playwright E2E (Python)

Use **Python 3.12** (3.14 may fail installing `greenlet` wheels on Windows).

```bash
# 1. Start app
cd app && ./gradlew bootRun

# 2. Setup (first time)
cd tests-web/python
py -3.12 -m venv .venv
# Windows:
.venv\Scripts\activate
pip install -r requirements.txt
playwright install chromium

# 3. Run desktop E2E
pytest

# Mobile Chrome smoke (Pixel 7 device profile — web DOM, not Appium)
pytest mobile_chrome -m smoke

# headed:
pytest --headed
```

Details: [`tests-web/python/README.md`](tests-web/python/README.md).

Events
New event
Event details