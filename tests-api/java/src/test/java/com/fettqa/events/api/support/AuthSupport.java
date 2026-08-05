package com.fettqa.events.api.support;

import static io.restassured.RestAssured.given;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class AuthSupport {

  public static final String ADMIN_EMAIL = "admin@example.com";
  public static final String ADMIN_PASSWORD = "admin123";

  private AuthSupport() {
  }

  public static String login(String email, String password) {
    return given()
        .filter(new AllureRestAssured())
        .contentType(ContentType.JSON)
        .body("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password))
        .when()
        .post("/api/auth/login")
        .then()
        .statusCode(200)
        .extract()
        .path("accessToken");
  }

  public static String adminToken() {
    return login(ADMIN_EMAIL, ADMIN_PASSWORD);
  }

  public static String registerUser(String email, String password) {
    return registerUser("Test User", email, password);
  }

  public static String registerUser(String fullName, String email, String password) {
    return given()
        .filter(new AllureRestAssured())
        .contentType(ContentType.JSON)
        .body("""
            {"fullName":"%s","email":"%s","password":"%s"}
            """.formatted(fullName, email, password))
        .when()
        .post("/api/auth/register")
        .then()
        .statusCode(201)
        .extract()
        .path("accessToken");
  }

  public static RequestSpecification givenAdmin() {
    return givenBearer(adminToken());
  }

  public static RequestSpecification givenBearer(String token) {
    return given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + token);
  }
}
