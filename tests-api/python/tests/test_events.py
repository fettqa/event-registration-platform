import allure


@allure.epic("Events API")
@allure.feature("Event Management")
class TestEvents:

  @allure.story("Create Event With Admin Token")
  def test_create_event_returns_201(self, client, admin_headers, unique_suffix):
    response = client.post(
        "/api/events",
        headers=admin_headers,
        json={
          "name": f"PyTest Event {unique_suffix}",
          "maxSeats": 10,
        },
    )
    assert response.status_code == 201, response.text
    body = response.json()
    assert body["id"] is not None
    assert body["name"] == f"PyTest Event {unique_suffix}"
    assert body["maxSeats"] == 10
    allure.attach(response.text, "response", allure.attachment_type.JSON)

  @allure.story("Create Event With Invalid Data")
  def test_create_event_invalid_returns_400(self, client, admin_headers):
    response = client.post(
        "/api/events",
        headers=admin_headers,
        json={
          "name": "",
          "maxSeats": 0,
        },
    )
    assert response.status_code == 400, response.text
    allure.attach(response.text, "response", allure.attachment_type.JSON)

  @allure.story("Get Event By ID")
  def test_get_event_by_id(self, client, created_event):
    event_id = created_event["id"]
    response = client.get(f"/api/events/{event_id}")
    assert response.status_code == 200, response.text
    assert response.json()["id"] == event_id
    allure.attach(response.text, "response", allure.attachment_type.JSON)

  @allure.story("Get Missing Event")
  def test_get_missing_event_returns_404(self, client):
    response = client.get("/api/events/999999")
    assert response.status_code == 404, response.text
    allure.attach(response.text, "response", allure.attachment_type.JSON)
