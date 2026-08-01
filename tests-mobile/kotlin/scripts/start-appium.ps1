# Start Appium with Android SDK env (required for UiAutomator2 session).
$sdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
if (-not (Test-Path $sdk)) {
    Write-Error "Android SDK not found at $sdk. Install Android Studio SDK or set ANDROID_HOME."
    exit 1
}

$env:ANDROID_HOME = $sdk
$env:ANDROID_SDK_ROOT = $sdk
$env:PATH = "$sdk\platform-tools;$sdk\emulator;$env:PATH"

Write-Host "ANDROID_HOME=$env:ANDROID_HOME"
Write-Host "Starting Appium on http://127.0.0.1:4723 ..."
appium
