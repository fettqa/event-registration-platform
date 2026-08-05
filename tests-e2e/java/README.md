# E2E Java (Playwright + Cucumber)

Black-box UI tests against a running app (`-DbaseUrl=…`, default: `http://localhost:8080`).

## Layout

| Path | Role |
|------|------|
| `functional/*E2ETest` | Classic JUnit 5 + Playwright |
| `cucumber/` + `resources/features/*.feature` | Cucumber BDD → same UI |

## Suites

| Task | Scope |
|------|--------|
| `./gradlew test` | Desktop Chromium (`functional/`) |
| `./gradlew mobileChromeTest` | **Mobile Chrome** smoke (Pixel 7 profile) |

`mobileChromeTest` is **not** Appium — same HTML DOM, mobile viewport + touch.

## Run

```bash
# terminal 1
cd app && ./gradlew bootRun

# terminal 2
cd tests-e2e/java
./gradlew installPlaywright
./gradlew test              # all E2E including Cucumber
./gradlew cucumber          # Cucumber scenarios only (not *E2ETest)
.\gradlew.bat mobileChromeTest # appium e2e
./gradlew test -DbaseUrl=http://localhost:8080
```

`./gradlew cucumber` must print scenarios (Given/When/Then). If you see `NO-SOURCE` and instant SUCCESS, the task did not run tests.

Allure results path (module root):

`tests-e2e/java/build/allure-results`  

(not the repo-root `build/`)

After a real run:

```bash
dir build\allure-results
allure serve build/allure-results
```

Allure: after `./gradlew test` or `./gradlew cucumber`:

```bash
allure serve build/allure-results
```

Cucumber scenarios appear via plugin `AllureCucumber7Jvm` (label `framework:cucumber`).  
JUnit `*E2ETest` classes appear via `allure-junit5`. Both write to the same `build/allure-results`.

Walkthrough: [`docs/tests-e2e-java-walkthrough.md`](../../docs/tests-e2e-java-walkthrough.md).
