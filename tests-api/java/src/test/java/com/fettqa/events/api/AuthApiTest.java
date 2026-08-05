package com.fettqa.events.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.fettqa.events.api.support.AuthSupport;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@Epic("Authentication and Authorization")
@Feature("Login / Create event permissions")
class AuthApiTest extends ApiTestBase {

  @Test
  @Story("Admin Login")
  void loginAdmin_returnsToken() {
    given().filter(new AllureRestAssured()).contentType(ContentType.JSON)
        .body("{\"email\":\"admin@example.com\",\"password\":\"admin123\"}")
        .when().post("/api/auth/login").then().statusCode(200)
        .body("accessToken", notNullValue()).body("tokenType", equalTo("Bearer"))
        .body("role", equalTo("ADMIN"));
  }

  @Test
  @Story("Guest can't create event")
  void createEvent_withoutToken_returns403() {
    given().filter(new AllureRestAssured()).contentType(ContentType.JSON)
        .body("{\"name\":\"%s\",\"maxSeats\":10}".formatted(unique("No Auth Event")))
        .when().post("/api/events").then().statusCode(403);
  }

  @Test
  @Story("User can't create event")
  void createEvent_withUserToken_returns403() {
    String token = AuthSupport.registerUser(uniqueEmail("user"), "secret12");
    AuthSupport.givenBearer(token).contentType(ContentType.JSON)
        .body("{\"name\":\"%s\",\"maxSeats\":10}".formatted(unique("User Event")))
        .when().post("/api/events").then().statusCode(403);
  }

  @Test
  @Story("Admin can create event")
  void createEvent_withAdminToken_returns201() {
    String name = unique("Admin Event");
    createEvent(adminToken(), name, 10).then().statusCode(201).body("name", equalTo(name));
  }
}
