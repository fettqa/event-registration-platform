# Android client — Event Registration Platform

Kotlin client for the Spring Boot API in this monorepo (same contract as the web UI / `tests-api` / k6).

Build walkthrough: [`docs/mobile-android-build-walkthrough.md`](../docs/mobile-android-build-walkthrough.md)  
Stack / QA mobile overview: [`docs/mobile-android-learning-guide.md`](../docs/mobile-android-learning-guide.md)

---

## Features (aligned with the web app)

| Feature | Who |
|---------|-----|
| Events list without login | guest |
| Search Events by name (Search / Clear) | everyone |
| Login / Register → JWT + fullName, email, role in header | — |
| Create Event (FAB) | `ADMIN`, `SUPER_USER` |
| Detail: seats left, registrations list, search by name | everyone (list is public) |
| Register for event | authenticated |
| Delete registration | `ADMIN`; `SUPER_USER` on own event; `USER` — own registration only |
| Delete event | `ADMIN`; `SUPER_USER` — own event only |
| Admin Panel (set USER / SUPER_USER) | `ADMIN` |
| Logout | clears local session |

Seeded admin: `admin@example.com` / `admin123`.

Package / applicationId: `com.fettqa.events.android`  
Launcher: `EventListActivity` (not Login).

---

## Open in Android Studio

1. **File → Open** → `event-registration-platform/android`
2. Wait for Gradle Sync
3. Emulator (Pixel, API 34+) or a physical device
4. Run ▶️ the `app` configuration

Debug APK path after build:

```text
android/app/build/outputs/apk/debug/app-debug.apk
```

```bash
cd android
./gradlew assembleDebug
```

---

## Configuring `BASE_URL`

Set in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"http://127.0.0.1:8080/\"")
```

After changing it — Sync / Rebuild, then Run or `assembleDebug` + install.

| Target | `BASE_URL` | Extra steps |
|--------|------------|-------------|
| **Local + emulator (recommended on Windows)** | `http://127.0.0.1:8080/` | `adb reverse tcp:8080 tcp:8080` before Run |
| **Local + emulator (no reverse)** | `http://10.0.2.2:8080/` | may be blocked by Windows Firewall |
| **Local + physical phone (Wi‑Fi)** | `http://<YOUR-PC-LAN-IP>:8080/` | same Wi‑Fi; allow port 8080 in firewall |
| **Render (HTTPS out of the box)** | `https://event-registration-jesq.onrender.com/` | no `adb reverse` |

Trailing slash is required for Retrofit.

Local HTTP uses `android:usesCleartextTraffic="true"`. Render (HTTPS) does not need cleartext.

### Fiddler / Charles (HTTPS)

Android 7+ apps do **not** trust user-installed CAs by default → `CertPathValidatorException: Trust anchor…` when proxying HTTPS.

Debug builds trust user CAs via `res/xml/network_security_config.xml` (`debug-overrides`). Also install the Fiddler/Charles root CA on the emulator.

Without a proxy: clear Wi‑Fi / `adb` proxy — Render HTTPS works with system CAs only.

---

### Local backend

```bash
cd app
./gradlew bootRun
# health: http://localhost:8080/actuator/health
```

Emulator + default `127.0.0.1`:

```powershell
& "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe" reverse tcp:8080 tcp:8080
```

Re-run reverse after **restarting the emulator**.

Install APK manually:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.fettqa.events.android/.ui.EventListActivity
```

---

### Render

1. In `app/build.gradle.kts` set:

   ```kotlin
   buildConfigField("String", "BASE_URL", "\"https://event-registration-jesq.onrender.com/\"")
   ```

   (or your Render service URL)

2. Rebuild / Run on emulator or phone — **no** `adb reverse`.

3. Free tier cold start: the first request may be slow; open the site in a browser to warm up if needed.

Login and API are the same as the web app on that host.

---

## Code layout (short)

```text
app/src/main/java/com/fettqa/events/android/
  model/     # JSON DTOs
  data/      # Retrofit APIs, SessionStore (+ JWT hydrate for role/email), ApiClient
  ui/        # EventList, Detail, Login, Register, CreateEvent, AdminPanel
```

Session (token, fullName, email, role) is stored in `SharedPreferences` (`erp_auth`), similar to web `localStorage`.  
If role/email are missing (older installs), they are read from the JWT payload.

---

## Tests

```bash
cd android
./gradlew test                         # MockWebServer, SessionStore, JWT hydrate
./gradlew connectedDebugAndroidTest    # Espresso smoke (emulator Online)
```

QA Automation (Maestro + Appium Kotlin/Python): see [`tests-mobile/README.md`](../tests-mobile/README.md)  
(run steps: emulator → `bootRun` → `adb reverse` → APK → Appium with `ANDROID_HOME` → `pytest` / `./gradlew test`).

---

## Docs index

Module walkthroughs: [`docs/README.md`](../docs/README.md).
