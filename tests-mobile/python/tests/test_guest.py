import allure
import pytest
from support.ui import assert_id_visible, assert_text_visible


@pytest.mark.smoke
@allure.epic("Mobile Appium")
@allure.feature("Guest")
def test_guest_sees_events(driver, wait):
    assert_text_visible(wait, "Events")
    assert_id_visible(wait, "headerLoginButton")
    assert_id_visible(wait, "headerRegisterButton")
    assert_id_visible(wait, "eventsSearchInput")
    assert_id_visible(wait, "eventsRecycler")
