# Appium + pytest (Python)

Minimal black-box UI suite for `com.fettqa.events.android`.

You will see the emulator UI while tests run.

## Tests

| Module | Scenario |
|--------|----------|
| `tests/test_guest.py` | Guest sees Events |
| `tests/test_login.py` | Admin login / bad password |
| `tests/test_register_user.py` | Register USER (no Create FAB) |
| `tests/test_register_for_event.py` | Register for event + duplicate error UI |

## How to run (Windows)

### One-time

```powershell
npm i -g appium
appium driver install uiautomator2

cd tests-mobile\python
py -3.12 -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
```

### Every session — 4 terminals / steps

**1. Emulator** — Android Studio Device Manager, or CLI:

```powershell
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
& "$sdk\emulator\emulator.exe" -list-avds          # e.g. Pixel_8
& "$sdk\emulator\emulator.exe" -avd Pixel_8 -netdelay none -netspeed full
adb wait-for-device
adb devices   # expect: device
```

**2. API + reverse**

```powershell
cd app
.\gradlew.bat bootRun
```

```powershell
adb reverse tcp:8080 tcp:8080
```

**3. APK** (if needed)

```powershell
cd android
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

**4. Appium** (must set Android SDK path)

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

Leave Appium running. Status: http://127.0.0.1:4723/status

**5. Pytest**

```powershell
cd tests-mobile\python
.\.venv\Scripts\activate
pytest -m smoke
pytest tests\test_guest.py -v
pytest
```

## Env

| Variable | Default |
|----------|---------|
| `APPIUM_URL` | `http://127.0.0.1:4723` |
| `ANDROID_APK` | `../../android/app/build/outputs/apk/debug/app-debug.apk` |
| `APPIUM_WAIT` | `15` (seconds) |

Allure: `allure serve allure-results`

## Troubleshooting

| Error | Fix |
|-------|-----|
| Connection to Appium refused | Start Appium (step 4) |
| `ANDROID_HOME` / `ANDROID_SDK_ROOT` not exported | Use `start-appium.ps1`, not bare `appium` |
| No device | Start emulator (CLI or Studio); `adb devices` |
| `Unknown AVD name […]` | `emulator -list-avds` → exact name (e.g. `Pixel_8`) |
| API errors in app | `bootRun` + `adb reverse` |

Parent overview: [`../README.md`](../README.md).
