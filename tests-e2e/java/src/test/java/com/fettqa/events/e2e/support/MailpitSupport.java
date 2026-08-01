package com.fettqa.events.e2e.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class MailpitSupport {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  private static final HttpClient CLIENT =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

  private MailpitSupport() {
  }

  public static String baseUrl() {
    String url = System.getenv("MAILPIT_URL");
    if (url == null || url.isBlank()) {
      url = System.getProperty("mailpitUrl", "http://localhost:8025");
    }
    return url.replaceAll("/$", "");
  }

  public static boolean available() {
    try {
      HttpRequest request =
          HttpRequest.newBuilder(URI.create(baseUrl() + "/api/v1/info"))
              .timeout(Duration.ofSeconds(2))
              .GET()
              .build();
      HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
      return response.statusCode() == 200;
    } catch (Exception ex) {
      return false;
    }
  }

  public static void deleteAllMessages() throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(baseUrl() + "/api/v1/messages"))
            .timeout(Duration.ofSeconds(10))
            .DELETE()
            .build();
    CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
  }

  public static JsonNode waitForMessage(String toEmail, String subjectContains, long timeoutMs)
      throws Exception {
    long deadline = System.currentTimeMillis() + timeoutMs;
    String query = URLEncoder.encode("to:" + toEmail, StandardCharsets.UTF_8);
    while (System.currentTimeMillis() < deadline) {
      HttpRequest request =
          HttpRequest.newBuilder(URI.create(baseUrl() + "/api/v1/search?query=" + query))
              .timeout(Duration.ofSeconds(5))
              .GET()
              .build();
      HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        JsonNode messages = MAPPER.readTree(response.body()).path("messages");
        if (messages.isArray()) {
          for (JsonNode msg : messages) {
            String subject = msg.path("Subject").asText("");
            if (subjectContains == null || subject.contains(subjectContains)) {
              return msg;
            }
          }
        }
      }
      Thread.sleep(500);
    }
    throw new AssertionError(
        "No Mailpit message for to=" + toEmail + " subject~" + subjectContains
            + " within " + timeoutMs + "ms");
  }
}
