import allure
import pytest
from appium.webdriver.common.appiumby import AppiumBy
from selenium.common.exceptions import TimeoutException
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait
from support.ui import APP_PACKAGE, assert_text_visible, tap_id, type_id


@pytest.mark.smoke
@allure.epic("Mobile Appium")
@allure.feature("Register")
def test_register_user(driver, wait, unique_suffix):
    tap_id(driver, wait, "headerRegisterButton")
    type_id(driver, wait, "fullNameInput", f"Mobile User {unique_suffix}")
    type_id(driver, wait, "emailInput", f"mobile_{unique_suffix}@example.com")
    type_id(driver, wait, "passwordInput", "secret12")
    tap_id(driver, wait, "registerButton")

    assert_text_visible(wait, "Events")
    assert_text_visible(wait, "Role: USER")

    short = WebDriverWait(driver, 3)
    with pytest.raises(TimeoutException):
        short.until(
            EC.visibility_of_element_located(
                (AppiumBy.ID, f"{APP_PACKAGE}:id/createEventFab")
            )
        )
