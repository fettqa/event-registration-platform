import allure
import pytest
from support.ui import (
    assert_id_visible,
    assert_text_visible,
    login_as_admin,
    tap_id,
    type_id,
)


@pytest.mark.smoke
@allure.epic("Mobile Appium")
@allure.feature("Login")
def test_login_admin(driver, wait):
    login_as_admin(driver, wait)
    assert_id_visible(wait, "headerAdminButton")
    assert_id_visible(wait, "createEventFab")


@pytest.mark.smoke
@allure.epic("Mobile Appium")
@allure.feature("Login")
def test_login_bad_password(driver, wait):
    tap_id(driver, wait, "headerLoginButton")
    type_id(driver, wait, "emailInput", "admin@example.com")
    type_id(driver, wait, "passwordInput", "wrong-password")
    tap_id(driver, wait, "loginButton")
    assert_id_visible(wait, "errorText")
    assert_text_visible(wait, "bad credentials")
