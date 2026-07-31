import httpx
import os
import sys
import pytest
import uuid
from pathlib import Path

# Ensure tests-api root is importable (kafka_support, etc.) when pytest is started from elsewhere.
_ROOT = Path(__file__).resolve().parent
if str(_ROOT) not in sys.path:
  sys.path.insert(0, str(_ROOT))

BASE_URL = os.getenv("BASE_URL", "http://localhost:8080")
ADMIN_EMAIL = os.getenv("ADMIN_EMAIL", "admin@example.com")
ADMIN_PASSWORD = os.getenv("ADMIN_PASSWORD", "admin123")


@pytest.fixture(scope="session")
def base_url() -> str:
  return BASE_URL.rstrip("/")


@pytest.fixture(scope="session")
def client(base_url: str):
  with httpx.Client(base_url=base_url, timeout=10.0) as c:
    yield c


@pytest.fixture(scope="session")
def admin_token(client: httpx.Client) -> str:
  response = client.post(
      "/api/auth/login",
      json={"email": ADMIN_EMAIL, "password": ADMIN_PASSWORD},
  )
  assert response.status_code == 200, response.text
  token = response.json()["accessToken"]
  assert token
  return token


@pytest.fixture(scope="session")
def admin_headers(admin_token: str) -> dict:
  return {"Authorization": f"Bearer {admin_token}"}


@pytest.fixture
def unique_suffix() -> str:
  return uuid.uuid4().hex[:8]


@pytest.fixture
def created_event(client: httpx.Client, admin_headers: dict, unique_suffix: str) -> dict:
  response = client.post(
      "/api/events",
      headers=admin_headers,
      json={
        "name": f"PyTest Event {unique_suffix}",
        "maxSeats": 10,
      },
  )
  assert response.status_code == 201, response.text
  return response.json()
