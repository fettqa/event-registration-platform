package com.fettqa.events.e2e.functional;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fettqa.events.e2e.base.PlaywrightBaseTest;
import com.fettqa.events.e2e.preconditions.Precondition;
import com.fettqa.events.e2e.support.UiAuth;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@Epic("End-to-End Tests")
@Feature("Create Event and Register")
public class CreateEventAndRegisterE2ETest extends PlaywrightBaseTest<Precondition> {

  @Test
  @Story("Admin can create an event and register for it")
  void createEventAndRegister_showsSuccess() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    String eventName = "E2E Java Event " + suffix;

    UiAuth.loginAsAdmin(page, baseUrl);
    openEventsList();
    createEventViaUi(eventName, 25);
    registerForEvent();
    assertRegistrationSuccess();
  }

  @Step("Open events list")
  private void openEventsList() {
    page.navigate(baseUrl + "/");
    assertThat(page.getByTestId("create-event-link")).isVisible();
  }

  @Step("Create event '{eventName}' with {seats} seats")
  private void createEventViaUi(String eventName, int seats) {
    page.getByTestId("create-event-link").click();
    assertThat(page.getByTestId("create-event-form")).isVisible();
    page.getByTestId("event-name-input").fill(eventName);
    page.getByTestId("event-seats-input").fill(String.valueOf(seats));
    page.getByTestId("submit-event").click();
    assertThat(page.getByTestId("event-title")).hasText(eventName);
  }

  @Step("Register for event")
  private void registerForEvent() {
    assertThat(page.getByTestId("register-user")).isVisible();
    assertThat(page.getByTestId("submit-registration")).isVisible();
    page.getByTestId("submit-registration").click();
  }

  @Step("Assert registration success")
  private void assertRegistrationSuccess() {
    assertThat(page.getByTestId("success-message")).isVisible();
    assertThat(page.getByTestId("success-message"))
        .containsText("Registration successful");
    assertTrue(
        page.getByTestId("registrations-table").textContent().contains("admin@example.com"));
  }
}
