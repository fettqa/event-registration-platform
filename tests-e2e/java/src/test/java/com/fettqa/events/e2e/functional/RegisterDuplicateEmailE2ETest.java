package com.fettqa.events.e2e.functional;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.fettqa.events.e2e.base.PlaywrightBaseTest;
import com.fettqa.events.e2e.preconditions.CreateEventPrecondition;
import com.fettqa.events.e2e.support.UiAuth;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class RegisterDuplicateEmailE2ETest extends PlaywrightBaseTest<CreateEventPrecondition> {

  @Test
  void registerSameEmailTwice_showsError() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    long eventId = precondition.createEvent("E2E Dup Event " + suffix, 10);

    UiAuth.loginAsAdmin(page, baseUrl);
    page.navigate(baseUrl + "/events/" + eventId);

    page.getByTestId("submit-registration").click();
    assertThat(page.getByTestId("success-message")).isVisible();

    page.getByTestId("submit-registration").click();
    assertThat(page.getByTestId("error-message")).isVisible();
  }
}
