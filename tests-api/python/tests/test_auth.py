import allure


@allure.epic("Authentication")
@allure.feature("Login / Create event permissions")
class TestAuth:

  @allure.story("Admin Login")
  def test_login_admin_returns_token(self, client):
    response = client.post(
        "/api/auth/login",
        json={"email": "admin@example.com", "password": "admin123"},
    )
    assert response.status_code == 200, response.text
    body = response.json()
    assert body["accessToken"]
    assert body["tokenType"] == "Bearer"
    assert body["role"] == "ADMIN"
    assert body["fullName"]
    allure.attach(response.text, "response", allure.attachment_type.JSON)

  @allure.story("Create Event Without Token")
  def test_create_event_without_token_returns_403(self, client, unique_suffix):
    response = client.post(
        "/api/events",
        json={"name": f"No Auth {unique_suffix}", "maxSeats": 10},
    )
    assert response.status_code == 403, response.text
    allure.attach(response.text, "response", allure.attachment_type.JSON)

  @allure.story("Create Event With User Token")
  def test_create_event_with_user_token_returns_403(self, client, unique_suffix):
    email = f"user_{unique_suffix}@example.com"
    register = client.post(
        "/api/auth/register",
        json={"fullName": "Python User", "email": email,
              "password": "secret12"},
    )
    assert register.status_code == 201, register.text
    token = register.json()["accessToken"]

    response = client.post(
        "/api/events",
        headers={"Authorization": f"Bearer {token}"},
        json={"name": f"User Event {unique_suffix}", "maxSeats": 10},
    )
    assert response.status_code == 403, response.text
    allure.attach(response.text, "response", allure.attachment_type.JSON)

  @allure.story("Create Event With Admin Token")
  def test_create_event_with_admin_token_returns_201(
      self, client, admin_headers, unique_suffix):
    response = client.post(
        "/api/events",
        headers=admin_headers,
        json={"name": f"Admin Event {unique_suffix}", "maxSeats": 10},
    )
    assert response.status_code == 201, response.text
    assert response.json()["name"] == f"Admin Event {unique_suffix}"
    allure.attach(response.text, "response", allure.attachment_type.JSON)
