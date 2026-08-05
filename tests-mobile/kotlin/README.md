# Appium + Kotlin

JUnit 5 + Appium Java client for `com.fettqa.events.android`.  
Sources: `src/test/kotlin/com/fettqa/events/mobile/`.

| Class | Scenario |
|-------|----------|
| `GuestEventsTest` | guest sees Events |
| `LoginTest` | admin login / bad password |
| `RegisterUserTest` | register USER (no Create FAB) |
| `RegisterForEventTest` | register for event + duplicate error |

## One-time

```powershell
npm i -g appium
appium driver install uiautomator2
```

## Each session

1. Emulator:

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

4. Appium (needs `ANDROID_HOME` / `ANDROID_SDK_ROOT`):

```powershell
cd tests-mobile\kotlin
.\scripts\start-appium.ps1
```

Status: http://127.0.0.1:4723/status

5. Tests:

```powershell
cd tests-mobile\kotlin
.\gradlew.bat test
.\gradlew.bat test --tests com.fettqa.events.mobile.GuestEventsTest
```

Default APK: `../../android/app/build/outputs/apk/debug/app-debug.apk`  
Override: `-DappiumUrl=…` `-Dapk=…`

## Troubleshooting

| Error | Fix |
|-------|-----|
| `ConnectException` `:4723` | `start-appium.ps1` |
| SDK env not exported | restart Appium via script |
| No device / unknown AVD | `-list-avds`, exact name |
| App cannot reach API | `bootRun` + `adb reverse` |

## Allure

```bash
./gradlew test
allure serve build/allure-results
```

CI Pages: `allure/mobile-kotlin/<run_number>/`.

Parent: [../README.md](../README.md).
