# Mobile QA

Black-box UI E2E against the Android APK and a running Spring API.

| Layer | Location | Role |
|-------|----------|------|
| Unit / Espresso | `android/` | white-box client tests |
| Mobile E2E | `tests-mobile/` | Maestro / Appium |
| Domain API | `tests-api`, Rest Assured | seats, roles, concurrency |

Mobile UI covers flows and that errors are shown. Full domain matrix stays in API tests.

## One-time setup

```powershell
npm i -g appium
appium driver install uiautomator2
# Maestro (optional): https://maestro.mobile.dev/
```

Appium needs `ANDROID_HOME` (usually `%LOCALAPPDATA%\Android\Sdk`).

## Shared preconditions

### Emulator

Android Studio Device Manager, or CLI:

```powershell
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
& "$sdk\emulator\emulator.exe" -list-avds
# use an exact name from the list, e.g. Pixel_8
& "$sdk\emulator\emulator.exe" -avd Pixel_8 -netdelay none -netspeed full

adb wait-for-device
adb devices
# optional: adb shell getprop sys.boot_completed  → 1
```

### API + reverse + APK

```powershell
cd app
.\gradlew.bat bootRun

adb reverse tcp:8080 tcp:8080   # again after emulator restart

cd android
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

Seeded admin: `admin@example.com` / `admin123`.

## Maestro

No Appium. Emulator + API + APK only.

```powershell
maestro test tests-mobile\maestro
maestro test tests-mobile\maestro\02_login_admin.yaml
```

Flows: `maestro/01_*.yaml` … `07_*.yaml`.

## Appium + Python

See [python/README.md](python/README.md).

```powershell
# terminal 1
cd tests-mobile\kotlin
.\scripts\start-appium.ps1
# http://127.0.0.1:4723/status → ready

# terminal 2 (after preconditions)
cd tests-mobile\python
py -3.12 -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
pytest -m smoke
```

## Appium + Kotlin

See [kotlin/README.md](kotlin/README.md).

```powershell
# terminal 1 — same start-appium.ps1 as above
cd tests-mobile\kotlin
.\scripts\start-appium.ps1

# terminal 2
.\gradlew.bat test
.\gradlew.bat test --tests com.fettqa.events.mobile.GuestEventsTest
```

Bare `appium` without SDK env fails with `Neither ANDROID_HOME nor ANDROID_SDK_ROOT…` — use the script.

## Troubleshooting

| Error | Fix |
|-------|-----|
| Connection refused `:4723` | start Appium, leave it running |
| `ANDROID_HOME` / `ANDROID_SDK_ROOT` not exported | `start-appium.ps1` |
| `SessionNotCreated` / no device | emulator online; `adb devices` → `device` |
| `Unknown AVD name` | `-list-avds`, use exact name |
| App cannot reach API | `bootRun` + `adb reverse tcp:8080 tcp:8080` |
| Stale UI | rebuild APK + `adb install -r …` |

## CI

| Suite | Workflow |
|-------|----------|
| Maestro | [mobile-maestro.yml](../.github/workflows/mobile-maestro.yml) |
| Appium Python | [mobile-appium-python.yml](../.github/workflows/mobile-appium-python.yml) |
| Appium Kotlin | [mobile-appium-kotlin.yml](../.github/workflows/mobile-appium-kotlin.yml) |

Jobs build API + APK, start the API, (Appium if needed), emulator, then `scripts/ci-run-suite.sh`. Allure for Appium suites: `allure/mobile-python|mobile-kotlin/<run>/` on Pages.

Manual: Actions → workflow → Run workflow.
