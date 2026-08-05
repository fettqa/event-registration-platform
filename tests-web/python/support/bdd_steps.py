"""Gherkin step definitions for pytest-bdd (shared by functional/test_bdd_*.py)."""

from __future__ import annotations

import uuid

import allure
import pytest
from playwright.sync_api import Page, expect
from pytest_bdd import given, parsers, then, when

from support.ui_auth import ADMIN_EMAIL, login_as_admin


@pytest.fixture
def bdd_ctx() -> dict:
  return {}


@given("I am logged in as admin")
@allure.step("Given I am logged in as admin")
def i_am_logged_in_as_admin(page: Page, base_url: str):
  login_as_admin(page, base_url)


@when("I open the events list")
@allure.step("When I open the events list")
def i_open_the_events_list(page: Page, base_url: str):
  page.goto(f"{base_url}/")
  expect(page.get_by_test_id("create-event-link")).to_be_visible()


@when(parsers.parse("I create an event with {seats:d} seats"))
@allure.step("When I create an event with seats")
def i_create_an_event_with_seats(page: Page, bdd_ctx: dict, seats: int):
  event_name = f"BDD Event {uuid.uuid4().hex[:8]}"
  bdd_ctx["event_name"] = event_name

  page.get_by_test_id("create-event-link").click()
  expect(page.get_by_test_id("create-event-form")).to_be_visible()
  page.get_by_test_id("event-name-input").fill(event_name)
  page.get_by_test_id("event-seats-input").fill(str(seats))
  page.get_by_test_id("submit-event").click()
  expect(page.get_by_test_id("event-title")).to_have_text(event_name)


@when("I register for the event")
@allure.step("When I register for the event")
def i_register_for_the_event(page: Page):
  expect(page.get_by_test_id("register-user")).to_be_visible()
  expect(page.get_by_test_id("submit-registration")).to_be_visible()
  page.get_by_test_id("submit-registration").click()


@then("I see registration success for admin")
@allure.step("Then I see registration success for admin")
def i_see_registration_success_for_admin(page: Page):
  expect(page.get_by_test_id("success-message")).to_be_visible()
  expect(page.get_by_test_id("success-message")).to_contain_text("Registration successful")
  expect(page.get_by_test_id("registrations-table")).to_contain_text(ADMIN_EMAIL)


@when("I register a new user via the UI")
@allure.step("When I register a new user via the UI")
def i_register_a_new_user_via_the_ui(page: Page, base_url: str, bdd_ctx: dict):
  suffix = uuid.uuid4().hex[:8]
  email = f"bdd_{suffix}@example.com"
  full_name = f"BDD User {suffix}"
  bdd_ctx["user_email"] = email
  bdd_ctx["user_full_name"] = full_name

  page.goto(f"{base_url}/register")
  expect(page.get_by_test_id("register-form")).to_be_visible()
  page.get_by_test_id("register-fullname-input").fill(full_name)
  page.get_by_test_id("register-email-input").fill(email)
  page.get_by_test_id("register-password-input").fill("secret12")
  page.get_by_test_id("register-password-confirm-input").fill("secret12")
  page.get_by_test_id("register-submit").click()


@then("the header shows me as a USER")
@allure.step("Then the header shows me as a USER")
def the_header_shows_me_as_a_user(page: Page, bdd_ctx: dict):
  expect(page.get_by_test_id("auth-full-name")).to_have_text(bdd_ctx["user_full_name"])
  expect(page.get_by_test_id("auth-email")).to_have_text(bdd_ctx["user_email"])
  expect(page.get_by_test_id("auth-role")).to_have_text("USER")
  expect(page.get_by_test_id("login-link")).to_be_hidden()
  expect(page.get_by_test_id("register-link")).to_be_hidden()
