import allure


def _register_user(client, unique_suffix, name="Python Tester"):
  email = f"user_{unique_suffix}@example.com"
  response = client.post(
      "/api/auth/register",
      json={"fullName": name, "email": email, "password": "secret12"},
  )
  assert response.status_code == 201, response.text
  return response.json()["accessToken"], email


@allure.epic("Registrations API")
@allure.feature("Event Registration")
class TestRegistrations:

  @allure.story("Register User for Event")
  def test_register_returns_201(self, client, created_event, unique_suffix):
    event_id = created_event["id"]
    token, email = _register_user(client, unique_suffix)
    response = client.post(
        f"/api/events/{event_id}/registrations",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert response.status_code == 201
    body = response.json()
    assert body["eventId"] == event_id
    assert body["email"] == email
    allure.attach(response.text, "response", allure.attachment_type.JSON)

  @allure.story("Register User for Event Without Token")
  def test_register_without_token_returns_401_or_403(self, client, created_event):
    response = client.post(f"/api/events/{created_event['id']}/registrations")
    assert response.status_code in (401, 403), response.text
    allure.attach(response.text, "response", allure.attachment_type.JSON)

  @allure.story("Register User for Event Duplicate Email")
  def test_register_duplicate_email_returns_409(
      self, client, created_event, unique_suffix):
    token, _ = _register_user(client, unique_suffix, "First")
    first = client.post(
        f"/api/events/{created_event['id']}/registrations",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert first.status_code == 201, first.text
    allure.attach(first.text, "response", allure.attachment_type.JSON)

    second = client.post(
        f"/api/events/{created_event['id']}/registrations",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert second.status_code == 409, second.text
    allure.attach(second.text, "response", allure.attachment_type.JSON)

  @allure.story("Register User for Event When Full")
  def test_register_when_full_returns_409(
      self, client, admin_headers, unique_suffix):
    event = client.post(
        "/api/events",
        headers=admin_headers,
        json={"name": f"Full Event {unique_suffix}", "maxSeats": 1},
    )
    assert event.status_code == 201, event.text
    event_id = event.json()["id"]

    token_a, _ = _register_user(client, f"a_{unique_suffix}", "A")
    token_b, _ = _register_user(client, f"b_{unique_suffix}", "B")

    first = client.post(
        f"/api/events/{event_id}/registrations",
        headers={"Authorization": f"Bearer {token_a}"},
    )
    assert first.status_code == 201
    allure.attach(first.text, "response", allure.attachment_type.JSON)
    second = client.post(
        f"/api/events/{event_id}/registrations",
        headers={"Authorization": f"Bearer {token_b}"},
    )
    assert second.status_code == 409
    allure.attach(second.text, "response", allure.attachment_type.JSON)

  @allure.story("Register User for Missing Event")
  def test_register_missing_event_returns_404(self, client, unique_suffix):
    token, _ = _register_user(client, unique_suffix)
    response = client.post(
        "/api/events/999999/registrations",
        headers={"Authorization": f"Bearer {token}"},
    )
    assert response.status_code == 404, response.text
    allure.attach(response.text, "response", allure.attachment_type.JSON)
