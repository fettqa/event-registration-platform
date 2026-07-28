from playwright.sync_api import Page, expect

ADMIN_EMAIL = "admin@example.com"
ADMIN_PASSWORD = "admin123"


def login_as_admin(page: Page, base_url: str) -> None:
  page.goto(f"{base_url}/login")
  expect(page.get_by_test_id("login-form")).to_be_visible()
  page.get_by_test_id("login-email-input").fill(ADMIN_EMAIL)
  page.get_by_test_id("login-password-input").fill(ADMIN_PASSWORD)
  page.get_by_test_id("login-submit").click()
  expect(page.get_by_test_id("auth-email")).to_be_visible()
  expect(page.get_by_test_id("auth-email")).to_have_text(ADMIN_EMAIL)
