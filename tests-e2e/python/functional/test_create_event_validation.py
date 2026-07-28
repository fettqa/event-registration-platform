from playwright.sync_api import Page, expect

from support.ui_auth import login_as_admin


def test_create_event_with_empty_name_stays_on_form(page: Page, base_url: str):
  login_as_admin(page, base_url)

  page.goto(f"{base_url}/events/new")
  page.get_by_test_id("event-name-input").fill("")
  page.get_by_test_id("event-seats-input").fill("0")
  page.get_by_test_id("submit-event").click()

  expect(page.get_by_test_id("create-event-form")).to_be_visible()
  expect(page.get_by_test_id("form-errors")).to_be_visible()
  expect(page.get_by_test_id("name-error")).to_be_visible()
  expect(page.get_by_test_id("seats-error")).to_be_visible()
