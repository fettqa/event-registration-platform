import os
import uuid

import pytest

BASE_URL = os.getenv("BASE_URL", "http://localhost:8080").rstrip("/")


@pytest.fixture(scope="session")
def base_url() -> str:
  return BASE_URL


@pytest.fixture
def unique_suffix() -> str:
  return uuid.uuid4().hex[:8]


@pytest.fixture(scope="session")
def browser_context_args(browser_context_args):
  """Optional: larger viewport for stable screenshots later."""
  return {
    **browser_context_args,
    "viewport": {"width": 1280, "height": 720},
  }