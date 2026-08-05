from playwright.sync_api import Page, expect
import allure
from support.ui_auth import login_as_admin


@allure.epic("End-to-End Tests")
@allure.feature("Create Event Validation")
@allure.story("Create Event Validation")
def test_create_event_with_empty_name_stays_on_form(page: Page, base_url: str):
  login_as_admin(page, base_url)

  with allure.step("Submit event with empty name and zero seats"):
    page.goto(f"{base_url}/events/new")
    page.get_by_test_id("event-name-input").fill("")
    page.get_by_test_id("event-seats-input").fill("0")
    page.get_by_test_id("submit-event").click()

  with allure.step("Assert create-event validation errors"):
    expect(page.get_by_test_id("create-event-form")).to_be_visible()
    expect(page.get_by_test_id("form-errors")).to_be_visible()
    expect(page.get_by_test_id("name-error")).to_be_visible()
    expect(page.get_by_test_id("seats-error")).to_be_visible()
