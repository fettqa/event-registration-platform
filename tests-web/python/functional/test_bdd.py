"""Load Gherkin features; step defs live in support.bdd_steps (via conftest pytest_plugins)."""

from pathlib import Path

import pytest
from pytest_bdd import scenarios

pytestmark = [pytest.mark.bdd]

FEATURES = Path(__file__).resolve().parents[1] / "features"

scenarios(FEATURES / "create_event_and_register.feature")
scenarios(FEATURES / "register_user.feature")
