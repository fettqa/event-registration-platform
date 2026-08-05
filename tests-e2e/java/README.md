# Playwright E2E (Java)

Against a running app (`-DbaseUrl`, default `http://localhost:8080`).

| Task | Scope |
|------|--------|
| `./gradlew test` | desktop Chromium (`functional/`) |
| `./gradlew mobileChromeTest` | Mobile Chrome smoke (Pixel 7 profile — web, not Appium) |

```powershell
cd app
.\gradlew.bat bootRun

cd ..\tests-e2e\java
.\gradlew.bat installPlaywright
.\gradlew.bat test
.\gradlew.bat mobileChromeTest
```
