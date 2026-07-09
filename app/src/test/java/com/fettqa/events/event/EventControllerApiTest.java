package com.fettqa.events.event;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.fettqa.events.Utils;
import com.fettqa.events.event.dto.EventResponse;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EventControllerApiTest {

  @LocalServerPort
  int port;

  private final String testDataPath = "testData/bulk_events.json";
  @Autowired
  private EventRepository eventRepository;

  @BeforeEach
  void setUp() {
    eventRepository.deleteAll();
    eventRepository.resetIdentity();
    RestAssured.port = port;
    RestAssured.basePath = "/api/events";
  }


  private Response createEvent(String name, Integer maxSeats) {
    return given()
        .contentType(ContentType.JSON)
        .body("""
            {"name":"%s","maxSeats":%d}
            """.formatted(name, maxSeats))
        .when()
        .post();
  }

  @Nested
  class EventLifecycle {

    @Test
    void createEvent_returns201() {
      createEvent("QA Conf", 50)
          .then()
          .statusCode(201)
          .body("id", notNullValue())
          .body("name", equalTo("QA Conf"));
    }

    @Test
    void patchEvent_returns200() {
      String newName = "Automation";
      Integer maxSeats = 50;
      Integer newMaxSeats = 100;
      Integer id = createEvent("QA Conf", maxSeats)
          .body()
          .path("id");

      given()
          .contentType(ContentType.JSON)
          .body("{\"name\":\"" + newName + "\"}")
          .when()
          .patch("{id}", id)
          .then()
          .statusCode(200)
          .body("name", equalTo(newName))
          .body("maxSeats", equalTo(maxSeats));

      given()
          .contentType(ContentType.JSON)
          .body("{\"maxSeats\":" + newMaxSeats + "}")
          .when()
          .patch("{id}", id)
          .then()
          .statusCode(200)
          .body("name", equalTo(newName))
          .body("maxSeats", equalTo(newMaxSeats));
    }

    @Test
    void getEventByName_returns200() {
      String eventName = "QA Conf";
      int maxSeats = 50;
      createEvent(eventName, maxSeats);

      given()
          .when()
          .queryParam("name", eventName)
          .get()
          .then()
          .statusCode(200)
          .body("[0].id", notNullValue())
          .body("[0].maxSeats", equalTo(maxSeats))
          .body("[0].name", equalTo(eventName))
          .body("size()", equalTo(1));
    }

    @Test
    void getEventById_returns200() {
      int maxSeats = 50;
      Integer id = createEvent("QA Conf", maxSeats)
          .body()
          .path("id");

      given()
          .when()
          .get("{id}", id)
          .then()
          .statusCode(200)
          .body("id", equalTo(id))
          .body("maxSeats", equalTo(maxSeats));
    }

    @Test
    void deleteEvent_returns204() {
      Integer id = createEvent("QA Conf", 999)
          .body()
          .path("id");

      given()
          .when()
          .delete("{id}", id)
          .then()
          .statusCode(204);

      given()
          .when()
          .get("{id}", id)
          .then()
          .statusCode(404);
    }

    @Test
    void getEventById_returns404WhenMissing() {
      given()
          .when()
          .get("{id}", 999)
          .then()
          .statusCode(404);
    }
  }


  @Test
  void createEventsBulk_returnsList() {
    Event[] events = Utils.jsonToObject(testDataPath, Event[].class);
    List<EventResponse> created = given()
        .contentType(ContentType.JSON)
        .body(getClass().getClassLoader().getResourceAsStream(testDataPath))
        .when()
        .post("bulk")
        .then()
        .statusCode(201)
        .body("size()", equalTo(events.length))
        .body("name", hasItem(events[0].getName()))
        .body("name", hasItem(events[1].getName()))
        .body("name", hasItem(events[2].getName()))
        .body("name", hasItem(events[3].getName()))
        .body("maxSeats", hasItem(events[0].getMaxSeats()))
        .body("maxSeats", hasItem(events[1].getMaxSeats()))
        .body("maxSeats", hasItem(events[2].getMaxSeats()))
        .body("maxSeats", hasItem(events[3].getMaxSeats()))
        .extract().jsonPath().getList("", EventResponse.class);

    given()
        .when()
        .delete("{id}", created.get(3).id())
        .then()
        .statusCode(204);

    given()
        .when()
        .get()
        .then()
        .statusCode(200)
        .body("size()", equalTo(events.length - 1))
        .body("name", hasItem(events[0].getName()))
        .body("name", hasItem(events[1].getName()))
        .body("name", hasItem(events[2].getName()))
        .body("maxSeats", hasItem(events[0].getMaxSeats()))
        .body("maxSeats", hasItem(events[1].getMaxSeats()))
        .body("maxSeats", hasItem(events[2].getMaxSeats()))
        .body(not(hasItem(events[3].getName())))
        .body(not(hasItem(events[3].getMaxSeats())));
  }
}
