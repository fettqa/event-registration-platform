# Playwright E2E (Java)

Web UI against a running Spring app (`-DbaseUrl=…`, default `http://localhost:8080`).

## Suites

| Task | Scope |
|------|--------|
| `./gradlew test` | Desktop Chromium (`functional/`) |
| `./gradlew mobileChromeTest` | **Mobile Chrome** smoke (Pixel 7 profile) |

`mobileChromeTest` is **not** Appium — same HTML DOM, mobile viewport + touch.

## Run

```powershell
cd app
.\gradlew.bat bootRun

cd ..\tests-e2e\java
.\gradlew.bat installPlaywright
.\gradlew.bat test
.\gradlew.bat mobileChromeTest
```
