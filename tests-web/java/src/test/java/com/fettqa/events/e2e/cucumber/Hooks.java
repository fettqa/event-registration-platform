package com.fettqa.events.e2e.cucumber;

import io.cucumber.java.After;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;

public class Hooks {

  private final PlaywrightWorld world;

  public Hooks(PlaywrightWorld world) {
    this.world = world;
  }

  @After
  public void afterScenario(Scenario scenario) {
    try {
      if (scenario.isFailed() && world.currentPageOrNull() != null) {
        Allure.getLifecycle()
            .addAttachment(
                "failure-screenshot",
                "image/png",
                "png",
                world.currentPageOrNull().screenshot());
      }
    } finally {
      world.closePage();
    }
  }
}
