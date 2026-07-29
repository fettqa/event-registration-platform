package com.fettqa.events.event.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.fettqa.events.event.Event;
import com.fettqa.events.event.dto.EventResponse;
import com.fettqa.events.utils.AuthTestSupport;
import com.fettqa.events.utils.TestDataCleaner;
import com.fettqa.events.utils.Utils;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
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
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Epic("Event Management")
@Feature("Lifecycle / Bulk Operations")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDataCleaner.class)
public class EventControllerApiTest {

  @LocalServerPort
  int port;

  @Autowired
  private TestDataCleaner testDataCleaner;

  private String adminToken;

  @BeforeEach
  void setUp() {
    testDataCleaner.cleanAndResetIds();
    RestAssured.port = port;
    RestAssured.basePath = "/api/events";
    adminToken = AuthTestSupport.adminToken(port);
  }

  private Response createEvent(String name, Integer maxSeats) {
    return given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + adminToken)
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
    @Story("Create Event")
    void createEvent_returns201() {
      createEvent("QA Conf", 50)
          .then()
          .statusCode(201)
          .body("id", notNullValue())
          .body("name", equalTo("QA Conf"));
    }

    @Test
    @Story("Patch Event")
    void patchEvent_returns200() {
      String newName = "Automation";
      Integer maxSeats = 50;
      Integer newMaxSeats = 100;
      Integer id = createEvent("QA Conf", maxSeats)
          .body()
          .path("id");

      given()
          .filter(new AllureRestAssured())
          .header("Authorization", "Bearer " + adminToken)
          .contentType(ContentType.JSON)
          .body("{\"name\":\"" + newName + "\"}")
          .when()
          .patch("{id}", id)
          .then()
          .statusCode(200)
          .body("name", equalTo(newName))
          .body("maxSeats", equalTo(maxSeats));

      given()
          .filter(new AllureRestAssured())
          .header("Authorization", "Bearer " + adminToken)
          .contentType(ContentType.JSON)
          .body("{\"maxSeats\":\"" + newMaxSeats + "\"}")
          .when()
          .patch("{id}", id)
          .then()
          .statusCode(200)
          .body("name", equalTo(newName))
          .body("maxSeats", equalTo(newMaxSeats));
    }

    @Test
    @Story("Patch Event - Invalid Data")
    void patchEvent_returns400() {
      Integer id = createEvent("QA Conf", 50)
          .then()
          .statusCode(201)
          .extract()
          .path("id");

      given()
          .filter(new AllureRestAssured())
          .header("Authorization", "Bearer " + adminToken)
          .contentType(ContentType.JSON)
          .body("{}")
          .when()
          .patch("{id}", id)
          .then()
          .statusCode(400);

      given()
          .filter(new AllureRestAssured())
          .header("Authorization", "Bearer " + adminToken)
          .contentType(ContentType.JSON)
          .body("{\"maxSeats\":-1}")
          .when()
          .patch("{id}", id)
          .then()
          .statusCode(400);
    }

    @Test
    @Story("Get Event by Name")
    void getEventByName_returns200() {
      String eventName = "QA Conf";
      int maxSeats = 50;
      createEvent(eventName, maxSeats);

      given()
          .filter(new AllureRestAssured())
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
    @Story("Get Event by ID")
    void getEventById_returns200() {
      int maxSeats = 50;
      Integer id = createEvent("QA Conf", maxSeats)
          .body()
          .path("id");

      given()
          .filter(new AllureRestAssured())
          .when()
          .get("{id}", id)
          .then()
          .statusCode(200)
          .body("id", equalTo(id))
          .body("maxSeats", equalTo(maxSeats));
    }

    @Test
    @Story("Get Event - Empty Result")
    void getEvent_returnsEmpty() {
      given()
          .filter(new AllureRestAssured())
          .when()
          .get()
          .then()
          .statusCode(200)
          .body("size()", equalTo(0));

      given()
          .filter(new AllureRestAssured())
          .when()
          .queryParam("name", "Unknown")
          .get()
          .then()
          .statusCode(200)
          .body("size()", equalTo(0));
    }

    @Test
    @Story("Delete Event")
    void deleteEvent_returns204() {
      Integer id = createEvent("QA Conf", 999)
          .body()
          .path("id");

      given()
          .filter(new AllureRestAssured())
          .header("Authorization", "Bearer " + adminToken)
          .when()
          .delete("{id}", id)
          .then()
          .statusCode(204);

      given()
          .filter(new AllureRestAssured())
          .when()
          .get("{id}", id)
          .then()
          .statusCode(404);
    }

    @Test
    @Story("Get Event by ID - Not Found")
    void getEventById_returns404WhenMissing() {
      given()
          .filter(new AllureRestAssured())
          .when()
          .get("{id}", 999)
          .then()
          .statusCode(404);
    }
  }

  @Nested
  class BulkEventCreation {

    @Test
    @Story("Create Bulk Events")
    void createEventsBulk_returnsList() {
      String testDataPath = "testData/bulk_events.json";
      Event[] events = Utils.jsonToObject(testDataPath, Event[].class);
      List<EventResponse> created = given()
          .filter(new AllureRestAssured())
          .header("Authorization", "Bearer " + adminToken)
          .contentType(ContentType.JSON)
          .body(Utils.jsonAsString(testDataPath))
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
          .filter(new AllureRestAssured())
          .header("Authorization", "Bearer " + adminToken)
          .when()
          .delete("{id}", created.get(3).id())
          .then()
          .statusCode(204);

      given()
          .filter(new AllureRestAssured())
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

    @Test
    @Story("Create Invalid Bulk Events")
    void createInvalidEventsBulk_returns400() {
      String testDataPath = "testData/bulk_invalid_events.json";
      String events = Utils.jsonAsString(testDataPath);
      given()
          .filter(new AllureRestAssured())
          .header("Authorization", "Bearer " + adminToken)
          .contentType(ContentType.JSON)
          .body(events)
          .when()
          .post("bulk")
          .then()
          .statusCode(400);
    }

    @Test
    @Story("Create Duplicated Bulk Events")
    void createDuplicatedEventsBulk_returns400() {
      String testDataPath = "testData/bulk_duplicated_events.json";
      String events = Utils.jsonAsString(testDataPath);
      given()
          .filter(new AllureRestAssured())
          .header("Authorization", "Bearer " + adminToken)
          .contentType(ContentType.JSON)
          .body(events)
          .when()
          .post("bulk")
          .then()
          .statusCode(400);
    }
  }
}
