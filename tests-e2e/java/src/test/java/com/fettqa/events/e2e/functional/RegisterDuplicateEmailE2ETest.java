package com.fettqa.events.e2e.functional;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.fettqa.events.e2e.base.PlaywrightBaseTest;
import com.fettqa.events.e2e.preconditions.CreateEventPrecondition;
import com.fettqa.events.e2e.support.UiAuth;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Step;
import io.qameta.allure.Story;
import java.util.UUID;
import org.junit.jupiter.api.Test;

@Epic("End-to-End Tests")
@Feature("Register Duplicate Email Validation")
public class RegisterDuplicateEmailE2ETest extends PlaywrightBaseTest<CreateEventPrecondition> {

  @Test
  @Story("Admin cannot register the same email twice for the same event")
  void registerSameEmailTwice_showsError() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    long eventId = precondition.createEvent("E2E Dup Event " + suffix, 10);

    UiAuth.loginAsAdmin(page, baseUrl);
    openEventPage(eventId);
    registerOnceSuccessfully();
    registerAgainAndExpectError();
  }

  @Step("Open event page {eventId}")
  private void openEventPage(long eventId) {
    page.navigate(baseUrl + "/events/" + eventId);
  }

  @Step("Register for event once")
  private void registerOnceSuccessfully() {
    page.getByTestId("submit-registration").click();
    assertThat(page.getByTestId("success-message")).isVisible();
  }

  @Step("Register again and expect duplicate error")
  private void registerAgainAndExpectError() {
    page.getByTestId("submit-registration").click();
    assertThat(page.getByTestId("error-message")).isVisible();
  }
}
