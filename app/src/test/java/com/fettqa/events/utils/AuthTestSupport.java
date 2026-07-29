package com.fettqa.events.utils;

import static io.restassured.RestAssured.given;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class AuthTestSupport {

  public static final String ADMIN_EMAIL = "admin@example.com";
  public static final String ADMIN_PASSWORD = "admin123";

  private AuthTestSupport() {
  }

  public static String login(int port, String email, String password) {
    return given()
        .filter(new AllureRestAssured())
        .port(port)
        .basePath("")
        .contentType(ContentType.JSON)
        .body("""
            {"email":"%s","password":"%s"}
            """.formatted(email, password))
        .when()
        .post("/api/auth/login")
        .then()
        .statusCode(200)
        .extract()
        .path("accessToken");
  }

  public static String adminToken(int port) {
    return login(port, ADMIN_EMAIL, ADMIN_PASSWORD);
  }

  public static String registerUser(int port, String email, String password) {
    return registerUser(port, "Test User", email, password);
  }

  public static String registerUser(int port, String fullName, String email, String password) {
    return given()
        .filter(new AllureRestAssured())
        .port(port)
        .basePath("")
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

  public static RequestSpecification givenAdmin(int port) {
    return given()
        .filter(new AllureRestAssured())
        .port(port)
        .header("Authorization", "Bearer " + adminToken(port));
  }

  public static RequestSpecification givenBearer(int port, String token) {
    return given()
        .filter(new AllureRestAssured())
        .port(port)
        .header("Authorization", "Bearer " + token);
  }
}
