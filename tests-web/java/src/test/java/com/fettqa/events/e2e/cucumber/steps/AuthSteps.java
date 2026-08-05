package com.fettqa.events.e2e.cucumber.steps;

import com.fettqa.events.e2e.cucumber.PlaywrightWorld;
import com.fettqa.events.e2e.support.UiAuth;
import io.cucumber.java.en.Given;

public class AuthSteps {

  private final PlaywrightWorld world;

  public AuthSteps(PlaywrightWorld world) {
    this.world = world;
  }

  @Given("I am logged in as admin")
  public void iAmLoggedInAsAdmin() {
    UiAuth.loginAsAdmin(world.page(), world.baseUrl());
  }
}
