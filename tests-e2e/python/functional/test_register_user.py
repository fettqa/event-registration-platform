from playwright.sync_api import Page, expect


def test_register_shows_user_in_header(page: Page, base_url: str, unique_suffix: str):
  email = f"ui_reg_{unique_suffix}@example.com"
  full_name = f"UI Reg {unique_suffix}"

  page.goto(f"{base_url}/register")
  expect(page.get_by_test_id("register-form")).to_be_visible()

  page.get_by_test_id("register-fullname-input").fill(full_name)
  page.get_by_test_id("register-email-input").fill(email)
  page.get_by_test_id("register-password-input").fill("secret12")
  page.get_by_test_id("register-password-confirm-input").fill("secret12")
  page.get_by_test_id("register-submit").click()

  expect(page.get_by_test_id("auth-full-name")).to_have_text(full_name)
  expect(page.get_by_test_id("auth-email")).to_have_text(email)
  expect(page.get_by_test_id("auth-role")).to_have_text("USER")
  expect(page.get_by_test_id("login-link")).to_be_hidden()
  expect(page.get_by_test_id("register-link")).to_be_hidden()
