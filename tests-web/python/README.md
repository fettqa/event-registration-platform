# Playwright E2E (Python)

Web UI against a running Spring app (`BASE_URL`, default `http://localhost:8080`).

## Suites

| Path | Browser | Role |
|------|---------|------|
| `functional/` | Desktop Chromium (1280×720) | Full E2E |
| `functional/test_bdd.py` + `features/` | Desktop | **pytest-bdd** Gherkin |
| `mobile_chrome/` | **Mobile Chrome** (Pixel 7 device profile) | Smoke only |

`mobile_chrome` is **not** Appium / native APK — same HTML DOM as desktop, mobile viewport + touch flags.

## Setup

```powershell
cd tests-web\python
py -3.12 -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
playwright install chromium
```

App must be running: `cd app && ./gradlew bootRun`.

## Run

```powershell
# Desktop (default testpaths) — includes BDD
pytest

# BDD only
pytest -m bdd

# Mobile Chrome smoke
pytest mobile_chrome -m smoke
```

Optional: `BASE_URL=https://…` for a remote stand.
