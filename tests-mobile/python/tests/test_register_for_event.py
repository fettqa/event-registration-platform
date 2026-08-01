import allure
import pytest
from support.ui import assert_id_visible, assert_text_visible, login_as_admin, tap_id, type_id


def _create_event(driver, wait, name: str, seats: str = "20"):
    tap_id(driver, wait, "createEventFab")
    type_id(driver, wait, "nameInput", name)
    type_id(driver, wait, "seatsInput", seats)
    tap_id(driver, wait, "submitButton")
    assert_id_visible(wait, "registerButton")


@pytest.mark.smoke
@allure.epic("Mobile Appium")
@allure.feature("Register for event")
def test_register_for_event_success(driver, wait, unique_suffix):
    login_as_admin(driver, wait)
    _create_event(driver, wait, f"Appium Event {unique_suffix}")
    tap_id(driver, wait, "registerButton")
    assert_text_visible(wait, "You are registered")
    assert_id_visible(wait, "registrationsRecycler")


@pytest.mark.smoke
@allure.epic("Mobile Appium")
@allure.feature("Register for event")
def test_register_duplicate_shows_error(driver, wait, unique_suffix):
    login_as_admin(driver, wait)
    _create_event(driver, wait, f"Appium Dup {unique_suffix}", seats="5")
    tap_id(driver, wait, "registerButton")
    assert_text_visible(wait, "You are registered")
    tap_id(driver, wait, "registerButton")
    assert_id_visible(wait, "errorText")
