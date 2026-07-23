from playwright.sync_api import Page, expect


def test_register_same_email_twice_shows_error(page: Page, base_url: str, unique_suffix: str):
  event_name = f"E2E Dup Event {unique_suffix}"
  email = f"dup_python_{unique_suffix}@example.com"

  page.goto(f"{base_url}/events/new")
  page.get_by_test_id("event-name-input").fill(event_name)
  page.get_by_test_id("event-seats-input").fill("10")
  page.get_by_test_id("submit-event").click()

  page.get_by_test_id("full-name-input").fill("First User")
  page.get_by_test_id("email-input").fill(email)
  page.get_by_test_id("submit-registration").click()
  expect(page.get_by_test_id("success-message")).to_be_visible()

  page.get_by_test_id("full-name-input").fill("Second User")
  page.get_by_test_id("email-input").fill(email)
  page.get_by_test_id("submit-registration").click()

  expect(page.get_by_test_id("error-message")).to_be_visible()