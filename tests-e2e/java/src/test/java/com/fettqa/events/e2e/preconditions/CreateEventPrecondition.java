package com.fettqa.events.e2e.preconditions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import java.util.Map;

public class CreateEventPrecondition extends Precondition {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final String ADMIN_EMAIL = "admin@example.com";
  private static final String ADMIN_PASSWORD = "admin123";

  public CreateEventPrecondition() {
  }

  public long createEvent(String name, int maxSeats) {
    String token = adminToken();
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
            .setHeader("Authorization", "Bearer " + token)
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

  private String adminToken() {
    String body;
    try {
      body = MAPPER.writeValueAsString(Map.of(
          "email", ADMIN_EMAIL,
          "password", ADMIN_PASSWORD));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to serialize login request", e);
    }

    APIResponse response = api.post(
        "/api/auth/login",
        RequestOptions.create()
            .setHeader("Content-Type", "application/json")
            .setData(body)
    );

    if (response.status() != 200) {
      throw new IllegalStateException(
          "Admin login failed. Status=" + response.status() + ", body=" + response.text());
    }

    try {
      JsonNode token = MAPPER.readTree(response.text()).get("accessToken");
      if (token == null || token.isNull()) {
        throw new IllegalStateException("Login response has no accessToken: " + response.text());
      }
      return token.asText();
    } catch (IllegalStateException e) {
      throw e;
    } catch (Exception e) {
      throw new IllegalStateException("Cannot parse accessToken from: " + response.text(), e);
    }
  }
}
