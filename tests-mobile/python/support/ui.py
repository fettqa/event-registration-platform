from appium.webdriver.common.appiumby import AppiumBy
from selenium.webdriver.support import expected_conditions as EC

APP_PACKAGE = "com.fettqa.events.android"


def rid(resource: str) -> str:
    return f"{APP_PACKAGE}:id/{resource}"


def tap_id(driver, wait, resource: str):
    el = wait.until(EC.element_to_be_clickable((AppiumBy.ID, rid(resource))))
    el.click()
    return el


def type_id(driver, wait, resource: str, text: str):
    el = wait.until(EC.visibility_of_element_located((AppiumBy.ID, rid(resource))))
    el.clear()
    el.send_keys(text)
    return el


def assert_id_visible(wait, resource: str):
    wait.until(EC.visibility_of_element_located((AppiumBy.ID, rid(resource))))


def assert_text_visible(wait, text: str):
    wait.until(
        EC.visibility_of_element_located(
            (AppiumBy.ANDROID_UIAUTOMATOR, f'new UiSelector().textContains("{text}")')
        )
    )


def login_as_admin(driver, wait):
    tap_id(driver, wait, "headerLoginButton")
    type_id(driver, wait, "emailInput", "admin@example.com")
    type_id(driver, wait, "passwordInput", "admin123")
    tap_id(driver, wait, "loginButton")
    assert_text_visible(wait, "admin@example.com")
    assert_text_visible(wait, "Role: ADMIN")
