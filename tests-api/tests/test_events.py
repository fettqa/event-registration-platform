def test_create_event_returns_201(client, unique_suffix):
  response = client.post("api/events",
                         json={
                           "name": f"PyTest Event {unique_suffix}",
                           "maxSeats": 10,
                         })
  assert response.status_code == 201, response.text
  body = response.json()
  assert body["id"] is not None
  assert body["name"] == f"PyTest Event {unique_suffix}"
  assert body["maxSeats"] == 10


def test_create_event_invalid_returns_400(client):
  response = client.post("api/events",
                         json={
                           "name": "",
                           "maxSeats": 0,
                         })
  assert response.status_code == 400, response.text


def test_get_event_by_id(client, created_event):
  event_id = created_event['id']
  response = client.get(f"api/events/{event_id}")
  assert response.status_code == 200, response.text
  assert response.json()['id'] == event_id


def test_get_missing_event_returns_404(client):
  response = client.get("api/events/999999")
  assert response.status_code == 404, response.text
