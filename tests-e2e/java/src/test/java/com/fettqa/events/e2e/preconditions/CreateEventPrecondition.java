package com.fettqa.events.e2e.preconditions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import java.util.Map;

public class CreateEventPrecondition extends Precondition {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public CreateEventPrecondition() {
  }

  public long createEvent(String name, int maxSeats) {
    String body;
    try {
      body = MAPPER.writeValueAsString(Map.of("name", name, "maxSeats", maxSeats));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to serialize create event request", e);
    }

    APIResponse response = api.post(
        "/api/events",
        RequestOptions.create()
            .setHeader("Content-Type", "application/json")
            .setData(body)
    );

    if (response.status() != 201) {
      throw new IllegalStateException(
          "Failed to create event. Status=" + response.status() + ", body=" + response.text());
    }

    try {
      JsonNode idNode = MAPPER.readTree(response.text()).get("id");
      if (idNode == null || idNode.isNull()) {
        throw new IllegalStateException("Response has no id field: " + response.text());
      }
      return idNode.asLong();
    } catch (IllegalStateException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException("Cannot parse event id from: " + response.text(), e);
    }
  }
}
