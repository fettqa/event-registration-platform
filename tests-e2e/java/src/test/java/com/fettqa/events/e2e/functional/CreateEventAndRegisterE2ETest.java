package com.fettqa.events.e2e.functional;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fettqa.events.e2e.base.PlaywrightBaseTest;
import com.fettqa.events.e2e.preconditions.Precondition;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class CreateEventAndRegisterE2ETest extends PlaywrightBaseTest<Precondition> {

  @Test
  void createEventAndRegister_showsSuccess() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    String eventName = "E2E Java Event " + suffix;
    String email = "e2e_java_" + suffix + "@example.com";

    page.navigate(baseUrl + "/");
    assertThat(page.getByTestId("create-event-link")).isVisible();

    page.getByTestId("create-event-link").click();
    assertThat(page.getByTestId("create-event-form")).isVisible();

    page.getByTestId("event-name-input").fill(eventName);
    page.getByTestId("event-seats-input").fill("25");
    page.getByTestId("submit-event").click();

    assertThat(page.getByTestId("event-title")).hasText(eventName);
    assertThat(page.getByTestId("register-form")).isVisible();

    page.getByTestId("full-name-input").fill("Java Playwright User");
    page.getByTestId("email-input").fill(email);
    page.getByTestId("submit-registration").click();

    assertThat(page.getByTestId("success-message")).isVisible();
    assertThat(page.getByTestId("success-message"))
        .containsText("Registration successful");

    assertTrue(
        page.getByTestId("registrations-table").textContent().contains(email));
  }
}