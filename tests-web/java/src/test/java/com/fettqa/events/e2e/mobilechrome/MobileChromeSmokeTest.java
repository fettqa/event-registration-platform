package com.fettqa.events.e2e.mobilechrome;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.fettqa.events.e2e.base.MobileChromePlaywrightBaseTest;
import com.fettqa.events.e2e.support.UiAuth;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.util.UUID;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Smoke E2E for the web UI in Mobile Chrome (Playwright Pixel 7 emulation)
 */
@Epic("Mobile Chrome Web")
@Feature("Smoke")
@Tag("mobile_chrome")
@Tag("smoke")
public class MobileChromeSmokeTest extends MobileChromePlaywrightBaseTest {

  @Test
  @Story("Guest sees events list")
  void guest_seesEventsList() {
    page.navigate(baseUrl + "/");
    assertThat(page.getByTestId("app-header")).isVisible();
    assertThat(page.getByTestId("login-link")).isVisible();
    assertThat(page.getByTestId("register-link")).isVisible();
    assertThat(page.getByTestId("events-table")).isVisible();
    assertThat(page.getByTestId("create-event-link")).isHidden();
  }

  @Test
  @Story("Admin login shows header")
  void login_admin_showsHeader() {
    UiAuth.loginAsAdmin(page, baseUrl);
    assertThat(page.getByTestId("auth-email")).hasText("admin@example.com");
    assertThat(page.getByTestId("auth-role")).hasText("ADMIN");
    assertThat(page.getByTestId("admin-panel-link")).isVisible();
    assertThat(page.getByTestId("login-link")).isHidden();
  }

  @Test
  @Story("Register user shows in header")
  void register_user_showsInHeader() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    String email = "mweb_" + suffix + "@example.com";
    String fullName = "Mobile Web " + suffix;

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
  }
}
