package com.fettqa.events.e2e.cucumber.steps;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.fettqa.events.e2e.cucumber.PlaywrightWorld;
import io.cucumber.java.en.When;
import java.util.UUID;

public class EventSteps {

  private final PlaywrightWorld world;

  public EventSteps(PlaywrightWorld world) {
    this.world = world;
  }

  @When("I open the events list")
  public void iOpenTheEventsList() {
    world.page().navigate(world.baseUrl() + "/");
    assertThat(world.page().getByTestId("create-event-link")).isVisible();
  }

  @When("I create an event with {int} seats")
  public void iCreateAnEventWithSeats(int seats) {
    String eventName = "Cucumber Event " + UUID.randomUUID().toString().substring(0, 8);
    world.setEventName(eventName);

    world.page().getByTestId("create-event-link").click();
    assertThat(world.page().getByTestId("create-event-form")).isVisible();
    world.page().getByTestId("event-name-input").fill(eventName);
    world.page().getByTestId("event-seats-input").fill(String.valueOf(seats));
    world.page().getByTestId("submit-event").click();
    assertThat(world.page().getByTestId("event-title")).hasText(eventName);
  }
}
