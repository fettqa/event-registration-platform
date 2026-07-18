def test_register_returns_201(client, created_event, unique_suffix):
  event_id = created_event['id']
  response = client.post(f"api/events/{event_id}/registrations",
                         json={
                           "fullName": "Python Tester",
                           "email": f"user_{unique_suffix}@example.com"
                         })
  assert response.status_code == 201
  body = response.json()
  assert body["eventId"] == event_id
  assert body["email"] == f"user_{unique_suffix}@example.com"


def test_register_duplicate_email_returns_409(client, created_event,
    unique_suffix):
  payload = {
    "fullName": "First",
    "email": f"user_{unique_suffix}@example.com"
  }
  first = client.post(f"api/events/{created_event['id']}/registrations",
                      json=payload)
  assert first.status_code == 201, first.text

  second = client.post(f"api/events/{created_event['id']}/registrations",
                       json={**payload, "fullName": "Second"})
  assert second.status_code == 409, second.text


def test_register_when_full_returns_409(client, unique_suffix):
  event = client.post(
      "/api/events",
      json={"name": f"Full Event {unique_suffix}", "maxSeats": 1},
  ).json()

  event_id = event["id"]
  first = client.post(
      f"/api/events/{event_id}/registrations",
      json={"email": f"a_{unique_suffix}@example.com", "fullName": "A"},
  )
  assert first.status_code == 201
  second = client.post(
      f"/api/events/{event_id}/registrations",
      json={"email": f"b_{unique_suffix}@example.com", "fullName": "B"},
  )
  assert second.status_code == 409


def test_register_missing_event_returns_404(client, unique_suffix):
  response = client.post("api/events/999999/registrations",
                         json={
                           "fullName": "Python Tester",
                           "email": f"user_{unique_suffix}@example.com"
                         })
  assert response.status_code == 404, response.text
