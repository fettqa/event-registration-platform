package com.fettqa.events.e2e.cucumber.steps;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.fettqa.events.e2e.cucumber.PlaywrightWorld;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.UUID;

public class UserSteps {

  private final PlaywrightWorld world;

  public UserSteps(PlaywrightWorld world) {
    this.world = world;
  }

  @When("I register a new user via the UI")
  public void iRegisterANewUserViaTheUi() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    String email = "cucumber_" + suffix + "@example.com";
    String fullName = "Cucumber User " + suffix;
    world.setUserEmail(email);
    world.setUserFullName(fullName);

    world.page().navigate(world.baseUrl() + "/register");
    assertThat(world.page().getByTestId("register-form")).isVisible();
    world.page().getByTestId("register-fullname-input").fill(fullName);
    world.page().getByTestId("register-email-input").fill(email);
    world.page().getByTestId("register-password-input").fill("secret12");
    world.page().getByTestId("register-password-confirm-input").fill("secret12");
    world.page().getByTestId("register-submit").click();
  }

  @Then("the header shows me as a USER")
  public void theHeaderShowsMeAsAUser() {
    assertThat(world.page().getByTestId("auth-full-name")).hasText(world.userFullName());
    assertThat(world.page().getByTestId("auth-email")).hasText(world.userEmail());
    assertThat(world.page().getByTestId("auth-role")).hasText("USER");
    assertThat(world.page().getByTestId("login-link")).isHidden();
    assertThat(world.page().getByTestId("register-link")).isHidden();
  }
}
