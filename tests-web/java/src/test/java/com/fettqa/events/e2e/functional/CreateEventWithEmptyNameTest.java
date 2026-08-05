package com.fettqa.events.e2e.functional;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.fettqa.events.e2e.base.PlaywrightBaseTest;
import com.fettqa.events.e2e.preconditions.Precondition;
import com.fettqa.events.e2e.support.UiAuth;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import org.junit.jupiter.api.Test;

@Epic("End-to-End Tests")
@Feature("Create Event Validation")
public class CreateEventWithEmptyNameTest extends PlaywrightBaseTest<Precondition> {

  @Test
  @Story("Admin cannot create an event with empty name and zero seats")
  void createEventWithEmptyName_showsValidation() {
    UiAuth.loginAsAdmin(page, baseUrl);
    submitInvalidEvent();
    assertValidationErrors();
  }

  @Step("Submit event with empty name and zero seats")
  private void submitInvalidEvent() {
    page.navigate(baseUrl + "/events/new");
    page.getByTestId("event-name-input").fill("");
    page.getByTestId("event-seats-input").fill("0");
    page.getByTestId("submit-event").click();
  }

  @Step("Assert create-event validation errors")
  private void assertValidationErrors() {
    assertThat(page.getByTestId("create-event-form")).isVisible();
    assertThat(page.getByTestId("form-errors")).isVisible();
    assertThat(page.getByTestId("name-error")).isVisible();
    assertThat(page.getByTestId("seats-error")).isVisible();
    assertThat(page).hasURL(java.util.regex.Pattern.compile(".*/events/new.*"));
  }
}
