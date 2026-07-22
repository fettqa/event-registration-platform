package com.fettqa.events.e2e.functional;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.fettqa.events.e2e.base.PlaywrightBaseTest;
import com.fettqa.events.e2e.preconditions.CreateEventPrecondition;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class RegisterDuplicateEmailE2ETest extends PlaywrightBaseTest<CreateEventPrecondition> {

  @Test
  void registerSameEmailTwice_showsError() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    String email = "dup_java_" + suffix + "@example.com";

    long eventId = precondition.createEvent("E2E Dup Event " + suffix, 10);

    page.navigate(baseUrl + "/events/" + eventId);

    page.getByTestId("full-name-input").fill("First User");
    page.getByTestId("email-input").fill(email);
    page.getByTestId("submit-registration").click();
    assertThat(page.getByTestId("success-message")).isVisible();

    page.getByTestId("full-name-input").fill("Second User");
    page.getByTestId("email-input").fill(email);
    page.getByTestId("submit-registration").click();

    assertThat(page.getByTestId("error-message")).hasText(
        "email " + email + " is already registered for event E2E Dup Event " + suffix);
  }
}
