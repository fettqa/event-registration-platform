"""Smoke E2E for the web UI in Mobile Chrome (Playwright device emulation).
"""

import allure
import pytest
from playwright.sync_api import Page, expect

from support.ui_auth import login_as_admin


@pytest.mark.smoke
@pytest.mark.mobile_chrome
@allure.epic("Mobile Chrome Web")
@allure.feature("Smoke")
def test_guest_sees_events_list(page: Page, base_url: str):
    page.goto(f"{base_url}/")
    expect(page.get_by_test_id("app-header")).to_be_visible()
    expect(page.get_by_test_id("login-link")).to_be_visible()
    expect(page.get_by_test_id("register-link")).to_be_visible()
    expect(page.get_by_test_id("events-table")).to_be_visible()
    expect(page.get_by_test_id("create-event-link")).to_be_hidden()


@pytest.mark.smoke
@pytest.mark.mobile_chrome
@allure.epic("Mobile Chrome Web")
@allure.feature("Smoke")
def test_login_admin_shows_header(page: Page, base_url: str):
    login_as_admin(page, base_url)
    expect(page.get_by_test_id("auth-email")).to_have_text("admin@example.com")
    expect(page.get_by_test_id("auth-role")).to_have_text("ADMIN")
    expect(page.get_by_test_id("admin-panel-link")).to_be_visible()
    expect(page.get_by_test_id("login-link")).to_be_hidden()


@pytest.mark.smoke
@pytest.mark.mobile_chrome
@allure.epic("Mobile Chrome Web")
@allure.feature("Smoke")
def test_register_user_shows_in_header(page: Page, base_url: str, unique_suffix: str):
    email = f"mweb_{unique_suffix}@example.com"
    full_name = f"Mobile Web {unique_suffix}"

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
