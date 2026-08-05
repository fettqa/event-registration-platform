# Playwright E2E (Python)

Against a running app (`BASE_URL`, default `http://localhost:8080`). Use Python **3.12** on Windows.

| Path | Browser | Role |
|------|---------|------|
| `functional/` | desktop Chromium | full E2E |
| `mobile_chrome/` | Mobile Chrome (Pixel 7) | smoke |

```powershell
cd app
.\gradlew.bat bootRun

cd ..\tests-e2e\python
py -3.12 -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
playwright install chromium

pytest
pytest mobile_chrome -m smoke
pytest --headed
```
