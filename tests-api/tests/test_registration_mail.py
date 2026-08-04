import allure
import pytest

from mailpit_support import MAILPIT_URL, delete_all_messages, mailpit_available, wait_for_message


@pytest.fixture(scope="module")
def require_mailpit():
  if not mailpit_available():
    pytest.skip(
        f"Mailpit is not available at {MAILPIT_URL}. "
        "Start: cd app && docker compose up -d kafka mailpit; "
        "run app with profiles kafka,mail"
    )


@allure.epic("Registrations API")
@allure.feature("Registration Mail")
@pytest.mark.mail
class TestRegistrationMail:

  @allure.story("Register for Event delivers confirmation email to Mailpit")
  def test_register_sends_confirmation_email(
      self, require_mailpit, client, created_event, unique_suffix):
    delete_all_messages()

    event_id = created_event["id"]
    event_name = created_event["name"]
    email = f"mail_user_{unique_suffix}@example.com"
    name = "Python Mail Tester"

    register = client.post(
        "/api/auth/register",
        json={"fullName": name, "email": email, "password": "secret12"},
    )
    assert register.status_code == 201, register.text
    token = register.json()["accessToken"]

    response = client.post(
        f"/api/events/{event_id}/registrations",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert response.status_code == 201, response.text
    allure.attach(response.text, "http-response", allure.attachment_type.JSON)

    message = wait_for_message(to_email=email, subject_contains=event_name)
    allure.attach(str(message), "mailpit-message", allure.attachment_type.JSON)

    assert event_name in (message.get("Subject") or "")
    to_addresses = [
        addr.get("Address", "")
        for addr in (message.get("To") or [])
    ]
    assert email in to_addresses
