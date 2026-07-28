package com.fettqa.events.e2e.functional;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.fettqa.events.e2e.base.PlaywrightBaseTest;
import com.fettqa.events.e2e.preconditions.Precondition;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class RegisterUserE2ETest extends PlaywrightBaseTest<Precondition> {

  @Test
  void register_showsUserInHeader() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    String email = "ui_reg_" + suffix + "@example.com";
    String fullName = "UI Reg " + suffix;

    page.navigate(baseUrl + "/register");
    assertThat(page.getByTestId("register-form")).isVisible();

    page.getByTestId("register-fullname-input").fill(fullName);
    page.getByTestId("register-email-input").fill(email);
    page.getByTestId("register-password-input").fill("secret12");
    page.getByTestId("register-password-confirm-input").fill("secret12");
    page.getByTestId("register-submit").click();

    assertThat(page.getByTestId("auth-full-name")).hasText(fullName);
    assertThat(page.getByTestId("auth-email")).hasText(email);
    assertThat(page.getByTestId("auth-role")).hasText("USER");
    assertThat(page.getByTestId("login-link")).isHidden();
    assertThat(page.getByTestId("register-link")).isHidden();
  }
}
