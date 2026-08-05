import os
import sys
import uuid
from pathlib import Path

import pytest
from appium import webdriver
from appium.options.android import UiAutomator2Options
from selenium.webdriver.support.ui import WebDriverWait

_ROOT = Path(__file__).resolve().parent
if str(_ROOT) not in sys.path:
    sys.path.insert(0, str(_ROOT))

from support.ui import APP_PACKAGE  # noqa: E402

APP_ACTIVITY = ".ui.EventListActivity"
APPIUM_URL = os.getenv("APPIUM_URL", "http://127.0.0.1:4723")
APK_PATH = os.getenv(
    "ANDROID_APK",
    str((_ROOT / ".." / ".." / "android" / "app" / "build" / "outputs" / "apk" / "debug" / "app-debug.apk").resolve()),
)
IMPLICIT_WAIT = float(os.getenv("APPIUM_WAIT", "15"))


@pytest.fixture
def unique_suffix() -> str:
    return uuid.uuid4().hex[:8]


@pytest.fixture
def driver():
    options = UiAutomator2Options()
    options.platform_name = "Android"
    options.automation_name = "UiAutomator2"
    options.app_package = APP_PACKAGE
    options.app_activity = APP_ACTIVITY
    options.no_reset = False
    options.full_reset = False
    options.new_command_timeout = 120
    if os.path.isfile(APK_PATH):
        options.app = APK_PATH

    drv = webdriver.Remote(APPIUM_URL, options=options)
    drv.implicitly_wait(IMPLICIT_WAIT)
    yield drv
    drv.quit()


@pytest.fixture
def wait(driver):
    return WebDriverWait(driver, IMPLICIT_WAIT)
