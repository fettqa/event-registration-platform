import os
import uuid
import allure
import pytest

# pytest-bdd registers steps as fixtures; only conftest / test modules are scanned.
pytest_plugins = ["support.bdd_steps"]

BASE_URL = os.getenv("BASE_URL", "http://localhost:8080").rstrip("/")


@pytest.fixture(scope="session")
def base_url() -> str:
  return BASE_URL


@pytest.fixture
def unique_suffix() -> str:
  return uuid.uuid4().hex[:8]


@pytest.fixture(scope="session")
def browser_context_args(browser_context_args):

  return {
    **browser_context_args,
    "viewport": {"width": 1280, "height": 720},
  }

@pytest.fixture(autouse=True)
def attach_screenshot_on_failure(request, page):
  yield
  if hasattr(request.node, "rep_call") and request.node.rep_call.failed:
    try:
      allure.attach(
          page.screenshot(),
          name="failure-screenshot",
          attachment_type=allure.attachment_type.PNG,
      )
    except Exception:
      pass


@pytest.hookimpl(tryfirst=True, hookwrapper=True)
def pytest_runtest_makereport(item, call):
  outcome = yield
  rep = outcome.get_result()
  setattr(item, "rep_" + rep.when, rep)
