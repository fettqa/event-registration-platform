package com.fettqa.events.event.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import com.fettqa.events.utils.AuthTestSupport;
import com.fettqa.events.utils.TestDataCleaner;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Epic("Permissions")
@Feature("Delete event/registration")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDataCleaner.class)
class DeletePermissionsApiTest {

  @LocalServerPort
  int port;

  @Autowired
  TestDataCleaner testDataCleaner;

  private String adminToken;

  @BeforeEach
  void setUp() {
    testDataCleaner.cleanAndResetIds();
    RestAssured.port = port;
    RestAssured.basePath = "";
    adminToken = AuthTestSupport.adminToken(port);
  }

  private Integer createEventAs(String token, String name) {
    return given()
        .header("Authorization", "Bearer " + token)
        .contentType(ContentType.JSON)
        .body("{\"name\":\"" + name + "\",\"maxSeats\":5}")
        .when()
        .post("/api/events")
        .then()
        .statusCode(201)
        .extract().path("id");
  }

  @Test
  @Story("Admin can delete any event")
  void admin_canDeleteAnyEvent() {
    Integer eventId = createEventAs(adminToken, "Admin Delete " + UUID.randomUUID().toString().substring(0, 6));

    given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .delete("/api/events/{id}", eventId)
        .then()
        .statusCode(204);
  }

  @Test
  @Story("User cannot delete events")
  void user_cannotDeleteEvent() {
    Integer eventId = createEventAs(adminToken, "User Cannot Delete " + UUID.randomUUID().toString().substring(0, 6));
    String userToken = AuthTestSupport.registerUser(
        port, "No Delete", "nodelete_" + UUID.randomUUID().toString().substring(0, 6) + "@example.com", "secret12");

    given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + userToken)
        .when()
        .delete("/api/events/{id}", eventId)
        .then()
        .statusCode(403);
  }

  @Test
  @Story("User can delete own registration")
  void user_canDeleteOwnRegistration() {
    Integer eventId = createEventAs(adminToken, "Own Reg " + UUID.randomUUID().toString().substring(0, 6));
    String email = "ownreg_" + UUID.randomUUID().toString().substring(0, 6) + "@example.com";
    String userToken = AuthTestSupport.registerUser(port, "Own Reg", email, "secret12");

    Integer registrationId = given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + userToken)
        .when()
        .post("/api/events/{id}/registrations", eventId)
        .then()
        .statusCode(201)
        .extract().path("id");

    given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + userToken)
        .when()
        .delete("/api/events/{eventId}/registrations/{registrationId}", eventId, registrationId)
        .then()
        .statusCode(204);

    given()
        .filter(new AllureRestAssured())
        .when()
        .get("/api/events/{id}/registrations", eventId)
        .then()
        .statusCode(200)
        .body("size()", equalTo(0));
  }

  @Test
  @Story("User cannot delete someone else's registration")
  void user_cannotDeleteSomeoneElsesRegistration() {
    Integer eventId = createEventAs(adminToken, "Foreign Reg " + UUID.randomUUID().toString().substring(0, 6));
    String ownerToken = AuthTestSupport.registerUser(
        port, "Owner", "owner_" + UUID.randomUUID().toString().substring(0, 6) + "@example.com", "secret12");
    String otherToken = AuthTestSupport.registerUser(
        port, "Other", "other_" + UUID.randomUUID().toString().substring(0, 6) + "@example.com", "secret12");

    Integer registrationId = given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + ownerToken)
        .when()
        .post("/api/events/{id}/registrations", eventId)
        .then()
        .statusCode(201)
        .extract().path("id");

    given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + otherToken)
        .when()
        .delete("/api/events/{eventId}/registrations/{registrationId}", eventId, registrationId)
        .then()
        .statusCode(403);
  }

  @Test
  @Story("Admin can delete any registration")
  void admin_canDeleteAnyRegistration() {
    Integer eventId = createEventAs(adminToken, "Admin Reg Del " + UUID.randomUUID().toString().substring(0, 6));
    String userToken = AuthTestSupport.registerUser(
        port, "Victim", "victim_" + UUID.randomUUID().toString().substring(0, 6) + "@example.com", "secret12");

    Integer registrationId = given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + userToken)
        .when()
        .post("/api/events/{id}/registrations", eventId)
        .then()
        .statusCode(201)
        .extract().path("id");

    given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + adminToken)
        .when()
        .delete("/api/events/{eventId}/registrations/{registrationId}", eventId, registrationId)
        .then()
        .statusCode(204);
  }
}
