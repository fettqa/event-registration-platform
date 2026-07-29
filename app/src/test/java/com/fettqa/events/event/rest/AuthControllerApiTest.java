package com.fettqa.events.event.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

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

@Epic("Authentication and Authorization")
@Feature("Login / Create event permissions")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDataCleaner.class)
class AuthControllerApiTest {

  @LocalServerPort
  int port;

  @Autowired
  TestDataCleaner testDataCleaner;

  @BeforeEach
  void setUp() {
    testDataCleaner.cleanAndResetIds();
    RestAssured.port = port;
    RestAssured.basePath = "";
  }

  @Test
  @Story("Admin Login")
  void loginAdmin_returnsToken() {
    given()
        .filter(new AllureRestAssured())
        .contentType(ContentType.JSON)
        .body("""
            {"email":"admin@example.com","password":"admin123"}
            """)
        .when()
        .post("/api/auth/login")
        .then()
        .statusCode(200)
        .body("accessToken", notNullValue())
        .body("tokenType", equalTo("Bearer"))
        .body("role", equalTo("ADMIN"));
  }

  @Test
  @Story("Guest can't create event")
  void createEvent_withoutToken_returns403() {
    given()
        .filter(new AllureRestAssured())
        .contentType(ContentType.JSON)
        .body("{\"name\":\"No Auth Event\",\"maxSeats\":10}")
        .when()
        .post("/api/events")
        .then()
        .statusCode(403);
  }

  @Test
  @Story("User can't create event")
  void createEvent_withUserToken_returns403() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    String userToken = AuthTestSupport.registerUser(
        port, "user_" + suffix + "@example.com", "secret12");

    AuthTestSupport.givenBearer(port, userToken)
        .contentType(ContentType.JSON)
        .body("{\"name\":\"User Event " + suffix + "\",\"maxSeats\":10}")
        .when()
        .post("/api/events")
        .then()
        .statusCode(403);
  }

  @Test
  @Story("Admin can create event")
  void createEvent_withAdminToken_returns201() {
    AuthTestSupport.givenAdmin(port)
        .contentType(ContentType.JSON)
        .body("{\"name\":\"Admin Event\",\"maxSeats\":10}")
        .when()
        .post("/api/events")
        .then()
        .statusCode(201)
        .body("name", equalTo("Admin Event"));
  }
}
