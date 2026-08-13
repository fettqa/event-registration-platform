# Mobile QA Automation

Black-box UI E2E against the Android APK + a running Spring API.

| Layer | Location | Role |
|-------|----------|------|
| Dev unit / Espresso | `android/` | White-box client tests |
| **QA mobile E2E** | `tests-mobile/` | Maestro / Appium (this folder) |
| Domain API | `tests-api` (Python / Java RestAssured) | 409 seats, roles, concurrency |

You **will see** taps on the emulator screen (Appium/Maestro drive a real device UI).

---

## One-time installs

```powershell
# Appium 2 + UiAutomator2 (for Appium Kotlin / Python)
npm i -g appium
appium driver install uiautomator2

# Maestro (optional YAML suite) — https://maestro.mobile.dev/
```

Android SDK is installed with Android Studio (typically  
`%LOCALAPPDATA%\Android\Sdk`). Appium must see it via `ANDROID_HOME`  
(see “Start Appium” below).

---

## Every run — shared preconditions

Keep these running / ready before Maestro or Appium tests:

### 1) Emulator Online

You can start the AVD from Android Studio (Device Manager ▶), or from the CLI (preferred for repeatable QA runs).

**List AVD names on this machine** (do not guess — names differ per install):

```powershell
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
& "$sdk\emulator\emulator.exe" -list-avds
```

Example output on this project machine: `Pixel_8`.

**Start emulator (CLI):**

```powershell
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
# replace Pixel_8 with a name from -list-avds
& "$sdk\emulator\emulator.exe" -avd Pixel_8 -netdelay none -netspeed full
```

Leave that terminal open. Wait until boot finishes, then:

```powershell
adb wait-for-device
adb devices
# expect: emulator-XXXX   device
```

Optional boot check:

```powershell
adb shell getprop sys.boot_completed
# expect: 1
```

If you see `Unknown AVD name […]`, the name is wrong — run `-list-avds` and use an exact match (e.g. `Pixel_8`, not `Pixel_8_API_34`).

### 2) Backend + port reverse

```powershell
# Terminal: API
cd app
.\gradlew.bat bootRun
```

```powershell
# Another terminal (local app BASE_URL is http://127.0.0.1:8080/)
adb reverse tcp:8080 tcp:8080
```

Re-run `adb reverse` after restarting the emulator.

### 3) Install APK

```powershell
cd android
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Seeded admin: `admin@example.com` / `admin123`.

---

## A) Maestro

Does **not** need Appium. Needs emulator + API + APK (steps above).

```powershell
# from repo root
maestro test tests-mobile\maestro
# one flow:
maestro test tests-mobile\maestro\02_login_admin.yaml

# JUnit / HTML report + failing-step screenshots
mkdir tests-mobile\maestro-results -Force
maestro test `
  --format html --output tests-mobile\maestro-results\index.html `
  --test-output-dir tests-mobile\maestro-results `
  --debug-output tests-mobile\maestro-results\debug `
  tests-mobile\maestro
```

Flows: `maestro/01_*.yaml` … `07_*.yaml` (guest, login, bad password, register, register-for-event, duplicate, search).

CI (`mobile-maestro.yml`):
- Artifact **`mobile-maestro-results`** (`index.html` + screenshots/logs)
- GitHub Pages: `…/maestro/<run_number>/` (job **Publish Maestro Report**)

---

## B) Appium + Python

Details: [`python/README.md`](python/README.md).

### Terminal 1 — Appium (with Android SDK env)

```powershell
cd tests-mobile\kotlin
.\scripts\start-appium.ps1
```

Or:

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
appium
```

Check: http://127.0.0.1:4723/status → `"ready": true`.

Leave this terminal open.

### Terminal 2 — pytest

(After emulator + `bootRun` + `adb reverse` + APK.)

```powershell
cd tests-mobile\python
py -3.12 -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt

pytest -m smoke
# or:
pytest tests\test_guest.py -v
pytest
```

Allure (optional): `allure serve allure-results`.

---

## C) Appium + Kotlin

Details: [`kotlin/README.md`](kotlin/README.md).

### Terminal 1 — Appium (same as Python)

```powershell
cd tests-mobile\kotlin
.\scripts\start-appium.ps1
```

### Terminal 2 — Gradle tests

```powershell
cd tests-mobile\kotlin
.\gradlew.bat test
.\gradlew.bat test --tests com.fettqa.events.mobile.GuestEventsTest
```

---

## Troubleshooting

| Error | Fix |
|-------|-----|
| `ConnectException` / cannot connect to `:4723` | Start Appium; keep that terminal open |
| `Neither ANDROID_HOME nor ANDROID_SDK_ROOT...` | Restart Appium via `start-appium.ps1` (not bare `appium` without env) |
| `SessionNotCreated` / no device | Emulator Online; `adb devices` → `device` |
| `Unknown AVD name […]` | `emulator -list-avds` → use exact name (e.g. `Pixel_8`) |
| App cannot reach API | `bootRun` + `adb reverse tcp:8080 tcp:8080` |
| APK missing / old UI | `cd android && ./gradlew assembleDebug` then `adb install -r …` |

---

## CI (GitHub Actions)

Separate workflow per suite (each can run independently / in parallel):

| Suite | Workflow | Paths that trigger |
|-------|----------|--------------------|
| Maestro | [`.github/workflows/mobile-maestro.yml`](../.github/workflows/mobile-maestro.yml) | `tests-mobile/maestro/**` (+ `app/`, `android/`) |
| Appium Python | [`.github/workflows/mobile-appium-python.yml`](../.github/workflows/mobile-appium-python.yml) | `tests-mobile/python/**` (+ `app/`, `android/`) |
| Appium Kotlin | [`.github/workflows/mobile-appium-kotlin.yml`](../.github/workflows/mobile-appium-kotlin.yml) | `tests-mobile/kotlin/**` (+ `app/`, `android/`) |

Each job: build API + APK → start API → (Appium if needed) → emulator → `scripts/ci-run-suite.sh <maestro|python|kotlin>`.

Appium Python / Kotlin also generate Allure HTML and publish to GitHub Pages (same pattern as E2E):

- `…/allure/mobile-python/<run_number>/`
- `…/allure/mobile-kotlin/<run_number>/`

Manual run: **Actions** → pick workflow → **Run workflow**.

---

## What stays out of mobile UI

Domain matrix (full 409 / concurrency / role matrix) → `tests-api` (Python / Java).  
Mobile E2E: user flows + that errors are **shown** on screen.
