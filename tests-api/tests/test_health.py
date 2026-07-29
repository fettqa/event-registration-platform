import allure

@allure.epic("Health")
@allure.feature("Health Check")
def test_health_is_up(client):
  response = client.get("/actuator/health")
  assert response.status_code == 200
  assert response.json()["status"] == "UP"
  allure.attach(response.text, "response", allure.attachment_type.JSON)
