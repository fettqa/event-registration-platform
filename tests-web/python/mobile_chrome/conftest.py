import pytest


@pytest.fixture(scope="session")
def browser_context_args(browser_context_args, playwright):
    """Mobile Chrome via Playwright device descriptor (Pixel 7)."""
    return {
        **browser_context_args,
        **playwright.devices["Pixel 7"],
    }
