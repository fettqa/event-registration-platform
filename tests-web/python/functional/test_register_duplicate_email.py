from playwright.sync_api import Page, expect
import allure
from support.ui_auth import login_as_admin


@allure.epic("End-to-End Tests")
@allure.feature("Register Duplicate Email Validation")
@allure.story("Register Duplicate Email")
def test_register_same_email_twice_shows_error(page: Page, base_url: str, unique_suffix: str):
  event_name = f"E2E Dup Event {unique_suffix}"

  login_as_admin(page, base_url)

  with allure.step(f"Create event '{event_name}'"):
    page.goto(f"{base_url}/events/new")
    page.get_by_test_id("event-name-input").fill(event_name)
    page.get_by_test_id("event-seats-input").fill("10")
    page.get_by_test_id("submit-event").click()

  with allure.step("Register for event once"):
    page.get_by_test_id("submit-registration").click()
    expect(page.get_by_test_id("success-message")).to_be_visible()

  with allure.step("Register again and expect duplicate error"):
    page.get_by_test_id("submit-registration").click()
    expect(page.get_by_test_id("error-message")).to_be_visible()
