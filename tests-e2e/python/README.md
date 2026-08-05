# E2E Python (Playwright + pytest-bdd)

Web UI against a running Spring app (`BASE_URL`, default `http://localhost:8080`).

## Suites

| Path | Browser | Role |
|------|---------|------|
| `functional/` | Desktop Chromium (1280×720) | Full E2E |
| `mobile_chrome/` | **Mobile Chrome** (Pixel 7 device profile) | Smoke only |

`mobile_chrome` is **not** Appium / native APK — same HTML DOM as desktop, mobile viewport + touch flags.

## Layout

| Path | Role |
|------|------|
| `functional/test_*.py` | Classic pytest + Playwright |
| `features/*.feature` | Gherkin scenarios |
| `functional/test_bdd.py` | Loads features (pytest-bdd) |
| `mobile_chrome/` | **Mobile Chrome** (Pixel 7 device profile) | Smoke only |
| `support/bdd_steps.py` | `@given` / `@when` / `@then` |
| `conftest.py` | `base_url`, screenshots; `pytest_plugins = ["support.bdd_steps"]` |

## Setup

```powershell
cd tests-e2e\python
py -3.12 -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
playwright install chromium
```

```bash
cd app && ./gradlew bootRun

cd tests-e2e/python
py -3.12 -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
playwright install chromium

pytest                         # all
pytest functional/test_bdd.py  # Gherkin only
pytest mobile_chrome -m smoke # Mobile Chrome smoke
pytest -m bdd
allure serve allure-results

```

App must be running: `cd app && ./gradlew bootRun`.

## Run

```powershell
# Desktop (default testpaths)
pytest

# Mobile Chrome smoke
pytest mobile_chrome -m smoke

# One test
pytest mobile_chrome/test_smoke.py::test_guest_sees_events_list
```

Optional: `BASE_URL=https://…` for a remote stand.

