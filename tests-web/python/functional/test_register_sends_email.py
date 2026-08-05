import allure
import pytest
from playwright.sync_api import Page, expect

from support.mailpit import MAILPIT_URL, delete_all_messages, mailpit_available, wait_for_message
from support.ui_auth import ADMIN_EMAIL, login_as_admin


@pytest.fixture(scope="module")
def require_mailpit():
  if not mailpit_available():
    pytest.skip(
        f"Mailpit is not available at {MAILPIT_URL}. "
        "Start kafka+mailpit and app with profiles kafka,mail"
    )


@allure.epic("End-to-End Tests")
@allure.feature("Registration Mail")
@pytest.mark.mail
@allure.story("UI registration for event delivers confirmation email")
def test_register_for_event_sends_confirmation_email(
    require_mailpit, page: Page, base_url: str, unique_suffix: str):
  delete_all_messages()
  event_name = f"E2E Mail Event {unique_suffix}"

  login_as_admin(page, base_url)

  with allure.step(f"Create event '{event_name}'"):
    page.goto(f"{base_url}/")
    page.get_by_test_id("create-event-link").click()
    page.get_by_test_id("event-name-input").fill(event_name)
    page.get_by_test_id("event-seats-input").fill("10")
    page.get_by_test_id("submit-event").click()
    expect(page.get_by_test_id("event-title")).to_have_text(event_name)

  with allure.step("Register for event via UI"):
    page.get_by_test_id("submit-registration").click()
    expect(page.get_by_test_id("success-message")).to_be_visible()

  with allure.step(f"Assert Mailpit received email for {ADMIN_EMAIL}"):
    message = wait_for_message(to_email=ADMIN_EMAIL, subject_contains=event_name)
    allure.attach(str(message), "mailpit-message", allure.attachment_type.JSON)
    assert event_name in (message.get("Subject") or "")
