# E2E Python (Playwright + pytest-bdd)

## Layout

| Path | Role |
|------|------|
| `functional/test_*.py` | Classic pytest + Playwright |
| `features/*.feature` | Gherkin scenarios |
| `functional/test_bdd.py` | Loads features (pytest-bdd) |
| `support/bdd_steps.py` | `@given` / `@when` / `@then` |
| `conftest.py` | `base_url`, screenshots; `pytest_plugins = ["support.bdd_steps"]` |

## Run

Use **Python 3.12** (CI uses 3.12). Python 3.14 often fails installing `greenlet`/`playwright` on Windows unless you install [MSVC Build Tools](https://visualstudio.microsoft.com/visual-cpp-build-tools/).

```bash
cd app && ./gradlew bootRun

cd tests-e2e/python
py -3.12 -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
playwright install chromium

pytest                         # all
pytest functional/test_bdd.py  # Gherkin only
pytest -m bdd
allure serve allure-results
```

Same scenario text as Java Cucumber under `tests-e2e/java/.../features/`.
