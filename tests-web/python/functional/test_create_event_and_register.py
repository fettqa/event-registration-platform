from playwright.sync_api import Page, expect
import allure
from support.ui_auth import login_as_admin


@allure.epic("End-to-End Tests")
@allure.feature("Create Event and Register")
@allure.story("Create Event and Register")
def test_create_event_and_register_shows_success(page: Page, base_url: str, unique_suffix: str):
  event_name = f"E2E Python Event {unique_suffix}"

  login_as_admin(page, base_url)

  with allure.step("Open events list"):
    page.goto(f"{base_url}/")
    expect(page.get_by_test_id("create-event-link")).to_be_visible()

  with allure.step(f"Create event '{event_name}' with 25 seats"):
    page.get_by_test_id("create-event-link").click()
    expect(page.get_by_test_id("create-event-form")).to_be_visible()
    page.get_by_test_id("event-name-input").fill(event_name)
    page.get_by_test_id("event-seats-input").fill("25")
    page.get_by_test_id("submit-event").click()
    expect(page.get_by_test_id("event-title")).to_have_text(event_name)

  with allure.step("Register for event"):
    expect(page.get_by_test_id("register-user")).to_be_visible()
    page.get_by_test_id("submit-registration").click()

  with allure.step("Assert registration success"):
    expect(page.get_by_test_id("success-message")).to_be_visible()
    expect(page.get_by_test_id("success-message")).to_contain_text("Registration successful")
    expect(page.get_by_test_id("registrations-table")).to_contain_text("admin@example.com")
