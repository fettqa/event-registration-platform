import httpx
import os
import pytest
import uuid

BASE_URL = os.getenv("BASE_URL", "http://localhost:8080")


@pytest.fixture(scope="session")
def base_url() -> str:
  return BASE_URL.rstrip("/")


@pytest.fixture(scope="session")
def client(base_url: str):
  with httpx.Client(base_url=base_url, timeout=10.0) as c:
    yield c


@pytest.fixture
def unique_suffix() -> str:
  return uuid.uuid4().hex[:8]


@pytest.fixture
def created_event(client: httpx.Client, unique_suffix: str) -> dict:
  response = client.post("/api/events",
                         json={
                           "name": f"PyTest Event {unique_suffix}",
                           "maxSeats": 10,
                         })
  assert response.status_code == 201, response.text
  return response.json()
