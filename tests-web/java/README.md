# Playwright E2E (Java)

Web UI against a running Spring app (`-DbaseUrl=…`, default `http://localhost:8080`).

## Suites

| Task | Scope |
|------|--------|
| `./gradlew test` | Desktop Chromium (`functional/`) + Cucumber BDD |
| `./gradlew cucumber` | Cucumber only (`features/` + `cucumber/`) |
| `./gradlew mobileChromeTest` | **Mobile Chrome** smoke (Pixel 7 profile) |

BDD: `src/test/resources/features/*.feature` → `com.fettqa.events.e2e.cucumber`.

`mobileChromeTest` is **not** Appium — same HTML DOM, mobile viewport + touch.

## Run

```powershell
cd app
.\gradlew.bat bootRun

cd ..\tests-web\java
.\gradlew.bat installPlaywright
.\gradlew.bat test
.\gradlew.bat cucumber
.\gradlew.bat mobileChromeTest
```
