#!/usr/bin/env bash
# Prepare device and run one suite: maestro | python | kotlin
# Preconditions: emulator Online, API on host :8080; for Appium suites — Appium on :4723.
set -euo pipefail

SUITE="${1:-}"
if [[ "$SUITE" != "maestro" && "$SUITE" != "python" && "$SUITE" != "kotlin" ]]; then
  echo "Usage: $0 maestro|python|kotlin" >&2
  exit 2
fi

ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
APK="${ANDROID_APK:-$ROOT/android/app/build/outputs/apk/debug/app-debug.apk}"
APPIUM_URL="${APPIUM_URL:-http://127.0.0.1:4723}"

echo "==> Suite: $SUITE"
echo "==> APK: $APK"
test -f "$APK"

echo "==> adb reverse + install"
adb wait-for-device
adb reverse tcp:8080 tcp:8080
adb install -r "$APK"

case "$SUITE" in
  maestro)
    command -v maestro >/dev/null
    RESULTS_DIR="$ROOT/tests-mobile/maestro-results"
    rm -rf "$RESULTS_DIR"
    mkdir -p "$RESULTS_DIR"
    # JUnit XML + failing-step screenshots / logs
    maestro test \
      --format junit \
      --output "$RESULTS_DIR/report.xml" \
      --test-output-dir "$RESULTS_DIR" \
      --debug-output "$RESULTS_DIR/debug" \
      "$ROOT/tests-mobile/maestro"
    ;;
  python)
    curl -sf "$APPIUM_URL/status" | grep -q ready
    cd "$ROOT/tests-mobile/python"
    export ANDROID_APK="$APK" APPIUM_URL
    pytest
    ;;
  kotlin)
    curl -sf "$APPIUM_URL/status" | grep -q ready
    cd "$ROOT/tests-mobile/kotlin"
    chmod +x gradlew
    ./gradlew test --no-daemon "-DappiumUrl=$APPIUM_URL" "-Dapk=$APK"
    ;;
esac

echo "==> Suite $SUITE passed"
