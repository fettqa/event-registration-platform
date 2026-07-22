package com.fettqa.events.e2e.functional;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.fettqa.events.e2e.base.PlaywrightBaseTest;
import com.fettqa.events.e2e.preconditions.Precondition;
import org.junit.jupiter.api.Test;

public class CreateEventWithEmptyNameTest extends PlaywrightBaseTest<Precondition> {

  @Test
  void createEventWithEmptyName_showsValidation() {
    page.navigate(baseUrl + "/events/new");
    page.getByTestId("event-name-input").fill("");
    page.getByTestId("event-seats-input").fill("0");
    page.getByTestId("submit-event").click();

    assertThat(page.getByTestId("create-event-form")).isVisible();
    assertThat(page.getByTestId("form-errors")).isVisible();
    assertThat(page.getByTestId("name-error")).isVisible();
    assertThat(page.getByTestId("seats-error")).isVisible();
    assertThat(page).hasURL(java.util.regex.Pattern.compile(".*/events$|.*/events/new.*"));
  }
}
