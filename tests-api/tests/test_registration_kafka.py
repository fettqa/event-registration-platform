import allure
import pytest

from kafka_support import BOOTSTRAP, kafka_available, wait_for_registration_event


def _register_user(client, unique_suffix, name="Python Kafka Tester"):
  email = f"kafka_user_{unique_suffix}@example.com"
  response = client.post(
      "/api/auth/register",
      json={"fullName": name, "email": email, "password": "secret12"},
  )
  assert response.status_code == 201, response.text
  return response.json()["accessToken"], email


@pytest.fixture(scope="module")
def require_kafka():
  try:
    ready = kafka_available(BOOTSTRAP)
  except ImportError as exc:
    pytest.skip(str(exc))
  if not ready:
    pytest.skip(f"Kafka is not available at {BOOTSTRAP}")


@allure.epic("Registrations API")
@allure.feature("Registration Kafka")
@pytest.mark.kafka
class TestRegistrationKafka:

  @allure.story("Register for Event publishes Kafka message")
  def test_register_publishes_registration_created_event(
      self, require_kafka, client, created_event, unique_suffix):
    event_id = created_event["id"]
    event_name = created_event["name"]
    token, email = _register_user(client, unique_suffix)

    response = client.post(
        f"/api/events/{event_id}/registrations",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert response.status_code == 201, response.text
    body = response.json()
    assert body["eventId"] == event_id
    assert body["email"] == email
    allure.attach(response.text, "http-response", allure.attachment_type.JSON)

    message = wait_for_registration_event(email=email, event_id=event_id)
    allure.attach(str(message), "kafka-message", allure.attachment_type.JSON)

    assert message["eventId"] == event_id
    assert message["eventName"] == event_name
    assert message["email"] == email
    assert message["fullName"] == "Python Kafka Tester"
    assert message.get("registrationId") is not None
