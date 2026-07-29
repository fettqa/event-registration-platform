package com.fettqa.events.e2e.support;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public final class UiAuth {

  public static final String ADMIN_EMAIL = "admin@example.com";
  public static final String ADMIN_PASSWORD = "admin123";

  private UiAuth() {
  }

  @Step("Login as admin")
  public static void loginAsAdmin(Page page, String baseUrl) {
    login(page, baseUrl, ADMIN_EMAIL, ADMIN_PASSWORD);
  }

  @Step("Login with email: {email}")
  public static void login(Page page, String baseUrl, String email, String password) {
    page.navigate(baseUrl + "/login");
    assertThat(page.getByTestId("login-form")).isVisible();
    page.getByTestId("login-email-input").fill(email);
    page.getByTestId("login-password-input").fill(password);
    page.getByTestId("login-submit").click();
    assertThat(page.getByTestId("auth-email")).isVisible();
    assertThat(page.getByTestId("auth-email")).hasText(email);
  }
}
