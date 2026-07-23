from playwright.sync_api import Page, expect


def test_create_event_and_register_shows_success(page: Page, base_url: str, unique_suffix: str):
  event_name = f"E2E Python Event {unique_suffix}"
  email = f"e2e_python_{unique_suffix}@example.com"

  page.goto(f"{base_url}/")
  expect(page.get_by_test_id("create-event-link")).to_be_visible()

  page.get_by_test_id("create-event-link").click()
  expect(page.get_by_test_id("create-event-form")).to_be_visible()

  page.get_by_test_id("event-name-input").fill(event_name)
  page.get_by_test_id("event-seats-input").fill("25")
  page.get_by_test_id("submit-event").click()

  expect(page.get_by_test_id("event-title")).to_have_text(event_name)
  expect(page.get_by_test_id("register-form")).to_be_visible()

  page.get_by_test_id("full-name-input").fill("Python Playwright User")
  page.get_by_test_id("email-input").fill(email)
  page.get_by_test_id("submit-registration").click()

  expect(page.get_by_test_id("success-message")).to_be_visible()
  expect(page.get_by_test_id("success-message")).to_contain_text("Registration successful")
  expect(page.get_by_test_id("registrations-table")).to_contain_text(email)