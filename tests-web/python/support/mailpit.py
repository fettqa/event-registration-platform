import os
import time
from typing import Any

import httpx

MAILPIT_URL = os.getenv("MAILPIT_URL", "http://localhost:8025").rstrip("/")


def mailpit_available(base_url: str = MAILPIT_URL, timeout_sec: float = 2.0) -> bool:
  try:
    response = httpx.get(f"{base_url}/api/v1/info", timeout=timeout_sec)
    return response.status_code == 200
  except (httpx.HTTPError, OSError):
    return False


def delete_all_messages(base_url: str = MAILPIT_URL) -> None:
  httpx.delete(f"{base_url}/api/v1/messages", timeout=10.0)


def wait_for_message(
    *,
    to_email: str,
    subject_contains: str | None = None,
    base_url: str = MAILPIT_URL,
    timeout_sec: float = 20.0,
) -> dict[str, Any]:
  deadline = time.time() + timeout_sec
  query = f"to:{to_email}"
  while time.time() < deadline:
    response = httpx.get(
        f"{base_url}/api/v1/search",
        params={"query": query},
        timeout=5.0,
    )
    response.raise_for_status()
    for msg in response.json().get("messages") or []:
      subject = msg.get("Subject") or ""
      if subject_contains is None or subject_contains in subject:
        return msg
    time.sleep(0.5)
  raise TimeoutError(
      f"No Mailpit message for to={to_email} subject~{subject_contains!r} within {timeout_sec}s"
  )
