# Appium + Kotlin (JUnit 5)

Minimal black-box UI suite for `com.fettqa.events.android`  
(Kotlin + Appium Java client + Gradle + Allure).

Sources: `src/test/kotlin/com/fettqa/events/mobile/`

You will see the emulator UI while tests run.

## Tests

| Class | Scenario |
|-------|----------|
| `GuestEventsTest` | Guest sees Events |
| `LoginTest` | Admin login / bad password |
| `RegisterUserTest` | Register USER (no Create FAB) |
| `RegisterForEventTest` | Register for event + duplicate error |

## How to run (Windows)

### One-time

```powershell
npm i -g appium
appium driver install uiautomator2
```

### Every session

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

**4. Appium with Android SDK env**

UiAutomator2 needs `ANDROID_HOME` / `ANDROID_SDK_ROOT`. Without them:

`Neither ANDROID_HOME nor ANDROID_SDK_ROOT environment variable was exported`

```powershell
cd tests-mobile\kotlin
.\scripts\start-appium.ps1
```

Or manually:

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
appium
```

Check: http://127.0.0.1:4723/status → `"ready": true`.  
Leave this terminal open.

**5. Gradle tests**

```powershell
cd tests-mobile\kotlin
.\gradlew.bat test
.\gradlew.bat test --tests com.fettqa.events.mobile.GuestEventsTest
.\gradlew.bat test "-DappiumUrl=http://127.0.0.1:4723" "-Dapk=C:/path/to/app-debug.apk"
```

Default APK (from this module): `../../android/app/build/outputs/apk/debug/app-debug.apk`

## Troubleshooting

| Error | Fix |
|-------|-----|
| `ConnectException` to `:4723` | Start Appium (`start-appium.ps1`) |
| `ANDROID_HOME` / `ANDROID_SDK_ROOT` not exported | Restart Appium **with** SDK env (script above) |
| `SessionNotCreated` / no device | Emulator Online; `adb devices` → `device` |
| `Unknown AVD name […]` | `emulator -list-avds` → exact name (e.g. `Pixel_8`) |
| App cannot reach API | `bootRun` + `adb reverse tcp:8080 tcp:8080` |

## Allure

Results: `build/allure-results` (after `./gradlew test`).

```bash
allure serve build/allure-results
```

CI publishes HTML to GitHub Pages under `allure/mobile-kotlin/<run_number>/`.

Parent overview: [`../README.md`](../README.md).
