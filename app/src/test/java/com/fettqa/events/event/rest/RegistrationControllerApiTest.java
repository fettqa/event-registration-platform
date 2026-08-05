package com.fettqa.events.event.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Epic("Event Registration")
@Feature("Register / List Operations")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDataCleaner.class)
public class RegistrationControllerApiTest {

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

  private Integer createEvent(int maxSeats) {
    return given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + adminToken)
        .contentType(ContentType.JSON)
        .body("{\"name\":\"QA Conf\",\"maxSeats\":\"" + maxSeats + "\"}")
        .when()
        .post("/api/events")
        .then()
        .statusCode(201)
        .extract().path("id");
  }

  private String registerToken(String fullName, String email) {
    return AuthTestSupport.registerUser(port, fullName, email, "secret12");
  }

  @Test
  @Story("Register for Event")
  void registration_returns201() {
    Integer eventId = createEvent(10);
    String token = registerToken("Ivan", "ivan@example.com");
    given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + token)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(201)
        .body("eventId", equalTo(eventId))
        .body("email", equalTo("ivan@example.com"))
        .body("fullName", equalTo("Ivan"));
  }

  @Test
  @Story("Register for Event without Token")
  void registration_withoutToken_returns401or403() {
    Integer eventId = createEvent(10);
    int status = given()
        .filter(new AllureRestAssured())
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .extract().statusCode();
    assertThat(status).isIn(401, 403);
  }

  @Test
  @Story("Get Registrations for Event")
  void get_registration_returns200() {
    Integer eventId = createEvent(10);
    String token = registerToken("Ivan", "ivan@example.com");
    given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + token)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(201);

    given()
        .filter(new AllureRestAssured())
        .when()
        .get("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(200)
        .body("size()", equalTo(1))
        .body("[0].email", equalTo("ivan@example.com"));
  }

  @Test
  @Story("Get Registrations for Non-Existent Event")
  void get_registration_returns404() {
    given()
        .filter(new AllureRestAssured())
        .when()
        .get("/api/events/{eventId}/registrations", 9999)
        .then()
        .statusCode(404);
  }

  @Test
  @Story("Get Registrations for Event with No Registrations")
  void get_registration_returns_empty_list() {
    Integer eventId = createEvent(10);
    given()
        .filter(new AllureRestAssured())
        .when()
        .get("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(200)
        .body("size()", equalTo(0));
  }

  @Test
  @Story("Search Registrations with Pagination")
  void search_registrations_returnsPagedResult() {
    Integer eventId = createEvent(10);
    String tokenA = registerToken("Alice Alpha", "alice@example.com");
    String tokenB = registerToken("Bob Beta", "bob@example.com");
    String tokenC = registerToken("Alice Gamma", "alice2@example.com");

    given().filter(new AllureRestAssured()).header("Authorization", "Bearer " + tokenA)
        .when().post("/api/events/{eventId}/registrations", eventId).then().statusCode(201);
    given().filter(new AllureRestAssured()).header("Authorization", "Bearer " + tokenB)
        .when().post("/api/events/{eventId}/registrations", eventId).then().statusCode(201);
    given().filter(new AllureRestAssured()).header("Authorization", "Bearer " + tokenC)
        .when().post("/api/events/{eventId}/registrations", eventId).then().statusCode(201);

    given()
        .filter(new AllureRestAssured())
        .queryParam("page", 0)
        .queryParam("size", 1)
        .queryParam("q", "Alice")
        .when()
        .get("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(200)
        .body("content.size()", equalTo(1))
        .body("totalElements", equalTo(2))
        .body("totalPages", equalTo(2))
        .body("number", equalTo(0))
        .body("content[0].fullName", equalTo("Alice Gamma"));
  }

  @Test
  @Story("Register for Event when Event is Full")
  void registration_returns409_whenEventIsFull() {
    Integer eventId = createEvent(1);
    String first = registerToken("Ivan", "ivan@example.com");
    String second = registerToken("John", "john@example.com");

    given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + first)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(201);

    given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + second)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(409);
  }

  @Test
  @Story("Register for Event with Already Registered Email")
  void registration_returns409_whenEmailAlreadyRegistered() {
    Integer eventId = createEvent(10);
    String token = registerToken("Ivan", "ivan@example.com");

    given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + token)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(201);

    given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + token)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(409);
  }

  @Test
  @Story("Concurrent Registration Respects Max Seats")
  void registration_concurrent_respectsMaxSeats() throws Exception {
    Integer eventId = createEvent(1);
    String token1 = registerToken("Ivan", "ivan_" + UUID.randomUUID().toString().substring(0, 6) + "@example.com");
    String token2 = registerToken("John", "john_" + UUID.randomUUID().toString().substring(0, 6) + "@example.com");

    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    ConcurrentLinkedQueue<Integer> statuses = new ConcurrentLinkedQueue<>();

    Consumer<String> register = (token) -> {
      ready.countDown();
      try {
        start.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
      int status = given()
          .filter(new AllureRestAssured())
          .header("Authorization", "Bearer " + token)
          .when()
          .post("/api/events/{eventId}/registrations", eventId)
          .then()
          .extract().statusCode();
      statuses.add(status);
    };

    Thread t1 = new Thread(() -> register.accept(token1));
    Thread t2 = new Thread(() -> register.accept(token2));
    t1.start();
    t2.start();

    ready.await();
    start.countDown();
    t1.join();
    t2.join();

    assertThat(statuses).containsExactlyInAnyOrder(201, 409);
    given()
        .filter(new AllureRestAssured())
        .when()
        .get("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(200)
        .body("size()", equalTo(1));
  }
}
