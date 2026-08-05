# Android client

Kotlin client for the Spring Boot API in this monorepo (same contract as the web UI / `tests-api` / k6).

- Build walkthrough: [docs/mobile-android-build-walkthrough.md](../docs/mobile-android-build-walkthrough.md)
- Stack / QA overview: [docs/mobile-android-learning-guide.md](../docs/mobile-android-learning-guide.md)

## Features

| Feature | Who |
|---------|-----|
| Events list without login | guest |
| Search + pagination (page size 10) | everyone |
| Login / Register → JWT in header | — |
| Create Event (FAB) | `ADMIN`, `SUPER_USER` |
| Detail: seats, registrations, search | everyone (list is public) |
| Register for event | authenticated |
| Delete registration | Admin; Super User on own event; User — own only |
| Delete event | Admin; Super User — own only |
| Admin Panel | `ADMIN` |
| Logout | clears local session |

Seeded admin: `admin@example.com` / `admin123`  
Package: `com.fettqa.events.android` — launcher `EventListActivity`.

## Open in Android Studio

1. File → Open → `android/`
2. Gradle Sync
3. Emulator (API 34+) or device → Run `app`

```bash
cd android && ./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## `BASE_URL`

In `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"http://127.0.0.1:8080/\"")
```

Sync / rebuild after changes. Trailing slash required for Retrofit.

| Target | `BASE_URL` | Notes |
|--------|------------|-------|
| Local + emulator | `http://127.0.0.1:8080/` | `adb reverse tcp:8080 tcp:8080` |
| Local without reverse | `http://10.0.2.2:8080/` | may hit Windows Firewall |
| Physical phone (Wi‑Fi) | `http://<PC-LAN-IP>:8080/` | same network; open port 8080 |
| Render | `https://event-registration-jesq.onrender.com/` | no reverse |

Local HTTP uses `usesCleartextTraffic=true`. HTTPS (Render) does not need it.

### Proxy (Fiddler / Charles)

Debug builds trust user CAs via `network_security_config.xml`. Install the tool’s root CA on the device. Without a proxy, clear any Wi‑Fi/`adb` proxy settings.

### Local backend

```bash
cd app && ./gradlew bootRun
```

```powershell
adb reverse tcp:8080 tcp:8080   # after each emulator restart
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.fettqa.events.android/.ui.EventListActivity
```

### Render

Set `BASE_URL` to the HTTPS service URL, rebuild, run — no `adb reverse`. Free tier cold start can be slow; hit the site in a browser first if needed.

## Layout

```text
app/src/main/java/com/fettqa/events/android/
  model/   JSON DTOs
  data/    Retrofit, SessionStore, ApiClient
  ui/      EventList, Detail, Login, Register, CreateEvent, AdminPanel
```

Session lives in `SharedPreferences` (`erp_auth`). Missing role/email is hydrated from the JWT payload.

## Tests

```bash
cd android
./gradlew test
./gradlew connectedDebugAndroidTest   # needs emulator
```

Maestro / Appium: [tests-mobile/README.md](../tests-mobile/README.md).

More guides: [docs/README.md](../docs/README.md).
