# Appium + pytest

UI suite for `com.fettqa.events.android`.

| Module | Scenario |
|--------|----------|
| `tests/test_guest.py` | guest sees Events |
| `tests/test_login.py` | admin login / bad password |
| `tests/test_register_user.py` | register USER (no Create FAB) |
| `tests/test_register_for_event.py` | register for event + duplicate error |

## One-time

```powershell
npm i -g appium
appium driver install uiautomator2

cd tests-mobile\python
py -3.12 -m venv .venv
.\.venv\Scripts\activate
pip install -r requirements.txt
```

## Each session

1. Emulator (Studio or CLI):

```powershell
$sdk = "$env:LOCALAPPDATA\Android\Sdk"
& "$sdk\emulator\emulator.exe" -list-avds
& "$sdk\emulator\emulator.exe" -avd Pixel_8 -netdelay none -netspeed full
adb wait-for-device
```

2. API + reverse:

```powershell
cd app
.\gradlew.bat bootRun
adb reverse tcp:8080 tcp:8080
```

3. APK (if needed):

```powershell
cd android
.\gradlew.bat assembleDebug
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

4. Appium with SDK path:

```powershell
cd tests-mobile\kotlin
.\scripts\start-appium.ps1
# or:
# $env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
# $env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
# appium
```

5. Tests:

```powershell
cd tests-mobile\python
.\.venv\Scripts\activate
pytest -m smoke
pytest tests\test_guest.py -v
```

## Env

| Variable | Default |
|----------|---------|
| `APPIUM_URL` | `http://127.0.0.1:4723` |
| `ANDROID_APK` | `../../android/app/build/outputs/apk/debug/app-debug.apk` |
| `APPIUM_WAIT` | `15` |

```bash
allure serve allure-results
```

CI Pages: `allure/mobile-python/<run_number>/`.

## Troubleshooting

| Error | Fix |
|-------|-----|
| Appium connection refused | step 4 |
| SDK env not exported | `start-appium.ps1` |
| No device / unknown AVD | `-list-avds`, exact name |
| API errors in app | `bootRun` + `adb reverse` |

Parent: [../README.md](../README.md).
