package com.fettqa.events.api;

import static io.restassured.RestAssured.given;

import com.fettqa.events.api.support.AuthSupport;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;

public abstract class ApiTestBase {

  @BeforeAll
  static void configureRestAssured() {
    RestAssured.baseURI = System.getProperty(
        "baseUrl", System.getenv().getOrDefault("BASE_URL", "http://localhost:8080"));
    RestAssured.basePath = "";
  }

  protected static String unique(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
  }

  protected static String uniqueEmail(String prefix) {
    return unique(prefix).toLowerCase() + "@example.com";
  }

  protected static Response createEvent(String adminToken, String name, int maxSeats) {
    return given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + adminToken)
        .contentType(ContentType.JSON)
        .body("{\"name\":\"%s\",\"maxSeats\":%d}".formatted(name, maxSeats))
        .when()
        .post("/api/events");
  }

  protected static Integer createEventId(String adminToken, String name, int maxSeats) {
    return createEvent(adminToken, name, maxSeats)
        .then()
        .statusCode(201)
        .extract()
        .path("id");
  }

  protected static String adminToken() {
    return AuthSupport.adminToken();
  }
}
