package com.fettqa.events.event.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fettqa.events.utils.TestDataCleaner;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
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

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDataCleaner.class)
public class RegistrationControllerApiTest {

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

  private Integer createEvent(int maxSeats) {
    return given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"QA Conf\",\"maxSeats\":" + maxSeats + "}")
        .when()
        .post("/api/events")
        .then()
        .statusCode(201)
        .extract().path("id");
  }

  @Test
  void registration_returns201() {
    Integer eventId = createEvent(10);
    given()
        .contentType(ContentType.JSON)
        .body("""
            
            {"email":"ivan@example.com","fullName":"Ivan"}
            """)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(201)
        .body("eventId", equalTo(eventId))
        .body("email", equalTo("ivan@example.com"));
  }

  @Test
  void get_registration_returns200() {
    Integer eventId = createEvent(10);
    given()
        .contentType(ContentType.JSON)
        .body("""
            {"email":"ivan@example.com",
            "fullName":"Ivan"}
            """)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(201);

    given()
        .when()
        .get("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(200)
        .body("size()", equalTo(1))
        .body("[0].email", equalTo("ivan@example.com"));
  }

  @Test
  void get_registration_returns404() {
    given()
        .when()
        .get("/api/events/{eventId}/registrations", 9999)
        .then()
        .statusCode(404);
  }

  @Test
  void get_registration_returns_empty_list() {
    Integer eventId = createEvent(10);
    given()
        .when()
        .get("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(200)
        .body("size()", equalTo(0));
  }

  @Test
  void registration_returns409_whenEventIsFull() {
    Integer eventId = createEvent(1);
    given()
        .contentType(ContentType.JSON)
        .body("""
            {"email":"ivan@example.com","fullName":"Ivan"}
            """)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body("""
            {"email":"john@example.com","fullName":"John"}
            """)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(409);
  }

  @Test
  void registration_returns409_whenEmailAlreadyRegistered() {
    Integer eventId = createEvent(10);
    given()
        .contentType(ContentType.JSON)
        .body("""
            {"email":"ivan@example.com","fullName":"Ivan"}
            """)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body("""
            {"email":"ivan@example.com","fullName":"Ivan"}
            """)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(409);
  }

  @Test
  void registration_concurrent_respectsMaxSeats() throws Exception {
    Integer eventId = createEvent(1);
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    ConcurrentLinkedQueue<Integer> statuses = new ConcurrentLinkedQueue<>();

    Consumer<String> register = (email) -> {
      ready.countDown();
      try {
        start.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      }
      int status = given()
          .contentType(ContentType.JSON)
          .body("{\"email\":\"" + email + "\",\"fullName\":\"User\"}")
          .when()
          .post("/api/events/{eventId}/registrations", eventId)
          .then()
          .extract().statusCode();
      statuses.add(status);
    };

    Thread t1 = new Thread(() -> register.accept("ivan@example.com"));
    Thread t2 = new Thread(() -> register.accept("john@example.com"));
    t1.start();
    t2.start();

    ready.await();
    start.countDown();
    t1.join();
    t2.join();

    assertThat(statuses).containsExactlyInAnyOrder(201, 409);
    given()
        .when()
        .get("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(200)
        .body("size()", equalTo(1));
  }
}
