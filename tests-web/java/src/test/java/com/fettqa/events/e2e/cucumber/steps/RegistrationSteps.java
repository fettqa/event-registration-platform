package com.fettqa.events.e2e.cucumber.steps;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fettqa.events.e2e.cucumber.PlaywrightWorld;
import com.fettqa.events.e2e.support.UiAuth;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class RegistrationSteps {

  private final PlaywrightWorld world;

  public RegistrationSteps(PlaywrightWorld world) {
    this.world = world;
  }

  @When("I register for the event")
  public void iRegisterForTheEvent() {
    assertThat(world.page().getByTestId("register-user")).isVisible();
    assertThat(world.page().getByTestId("submit-registration")).isVisible();
    world.page().getByTestId("submit-registration").click();
  }

  @Then("I see registration success for admin")
  public void iSeeRegistrationSuccessForAdmin() {
    assertThat(world.page().getByTestId("success-message")).isVisible();
    assertThat(world.page().getByTestId("success-message"))
        .containsText("Registration successful");
    assertTrue(
        world.page().getByTestId("registrations-table").textContent()
            .contains(UiAuth.ADMIN_EMAIL));
  }
}
