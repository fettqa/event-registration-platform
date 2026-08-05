package com.fettqa.events.api.support;

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
    return System.getProperty(
        "mailpitUrl", System.getenv().getOrDefault("MAILPIT_URL", "http://localhost:8025"))
        .replaceAll("/$", "");
  }

  public static boolean available() {
    try {
      HttpResponse<String> response = CLIENT.send(request("/api/v1/info").GET().build(),
          HttpResponse.BodyHandlers.ofString());
      return response.statusCode() == 200;
    } catch (Exception ex) {
      return false;
    }
  }

  public static void deleteAllMessages() throws Exception {
    CLIENT.send(request("/api/v1/messages").DELETE().build(), HttpResponse.BodyHandlers.ofString());
  }

  public static JsonNode waitForMessage(String toEmail, String subjectContains, Duration timeout)
      throws Exception {
    long deadline = System.nanoTime() + timeout.toNanos();
    String query = URLEncoder.encode("to:" + toEmail, StandardCharsets.UTF_8);
    while (System.nanoTime() < deadline) {
      HttpResponse<String> response = CLIENT.send(
          request("/api/v1/search?query=" + query).GET().build(),
          HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        for (JsonNode message : MAPPER.readTree(response.body()).path("messages")) {
          if (message.path("Subject").asText("").contains(subjectContains)) {
            return message;
          }
        }
      }
      Thread.sleep(500);
    }
    throw new AssertionError("No Mailpit message for " + toEmail + " within " + timeout);
  }

  private static HttpRequest.Builder request(String path) {
    return HttpRequest.newBuilder(URI.create(baseUrl() + path)).timeout(Duration.ofSeconds(5));
  }
}
