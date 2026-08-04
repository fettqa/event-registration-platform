package com.fettqa.events.e2e.functional;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.fettqa.events.e2e.base.PlaywrightBaseTest;
import com.fettqa.events.e2e.preconditions.Precondition;
import com.fettqa.events.e2e.support.MailpitSupport;
import com.fettqa.events.e2e.support.UiAuth;
import com.fasterxml.jackson.databind.JsonNode;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@Epic("End-to-End Tests")
@Feature("Registration Mail")
public class RegisterSendsEmailE2ETest extends PlaywrightBaseTest<Precondition> {

  @Test
  @Story("UI registration for event delivers confirmation email")
  void registerForEvent_sendsConfirmationEmail() throws Exception {
    assumeTrue(
        MailpitSupport.available(),
        () -> "Mailpit is not available at " + MailpitSupport.baseUrl()
            + ". Start kafka+mailpit and app with profiles kafka,mail");

    MailpitSupport.deleteAllMessages();
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    String eventName = "E2E Mail Event " + suffix;

    UiAuth.loginAsAdmin(page, baseUrl);
    createEventViaUi(eventName);
    registerForEvent();
    assertMailpitMessage(eventName);
  }

  @Step("Create event '{eventName}'")
  private void createEventViaUi(String eventName) {
    page.navigate(baseUrl + "/");
    page.getByTestId("create-event-link").click();
    page.getByTestId("event-name-input").fill(eventName);
    page.getByTestId("event-seats-input").fill("10");
    page.getByTestId("submit-event").click();
    assertThat(page.getByTestId("event-title")).hasText(eventName);
  }

  @Step("Register for event via UI")
  private void registerForEvent() {
    page.getByTestId("submit-registration").click();
    assertThat(page.getByTestId("success-message")).isVisible();
  }

  @Step("Assert Mailpit received email for admin")
  private void assertMailpitMessage(String eventName) throws Exception {
    JsonNode message =
        MailpitSupport.waitForMessage(UiAuth.ADMIN_EMAIL, eventName, 20_000);
    assertTrue(message.path("Subject").asText().contains(eventName));
  }
}
