package com.fettqa.events.event.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import com.fettqa.events.event.EventRepository;
import com.fettqa.events.registration.RegistrationRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegistrationControllerApiTest {

  @LocalServerPort
  int port;
  @Autowired
  EventRepository eventRepository;
  @Autowired
  RegistrationRepository registrationRepository;

  @BeforeEach
  void setUp() {
    registrationRepository.deleteAll();
    registrationRepository.resetIdentity();
    eventRepository.deleteAll();
    eventRepository.resetIdentity();
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
  void register_returns201() {
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
  void register_returns409_whenEventIsFull() {
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
  void register_returns409_whenEmailAlreadyRegistered() {
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
}
