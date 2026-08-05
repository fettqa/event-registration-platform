package com.fettqa.events.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import com.fettqa.events.api.support.AuthSupport;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@Epic("Event Management")
@Feature("Lifecycle / Bulk Operations")
class EventApiTest extends ApiTestBase {

  @Test @Story("Create Event")
  void createEvent_returns201() {
    String name = unique("QA Conf");
    createEvent(adminToken(), name, 50).then().statusCode(201)
        .body("id", notNullValue()).body("name", equalTo(name));
  }

  @Test @Story("Patch Event")
  void patchEvent_returns200() {
    String admin = adminToken();
    int seats = 50;
    Integer id = createEventId(admin, unique("Patch Event"), seats);
    String updated = unique("Automation");
    AuthSupport.givenBearer(admin).contentType(ContentType.JSON).body("{\"name\":\"%s\"}".formatted(updated))
        .when().patch("/api/events/{id}", id).then().statusCode(200).body("name", equalTo(updated))
        .body("maxSeats", equalTo(seats));
    AuthSupport.givenBearer(admin).contentType(ContentType.JSON).body("{\"maxSeats\":100}")
        .when().patch("/api/events/{id}", id).then().statusCode(200).body("maxSeats", equalTo(100));
  }

  @Test @Story("Patch Event - Invalid Data")
  void patchEvent_returns400() {
    String admin = adminToken();
    Integer id = createEventId(admin, unique("Invalid Patch"), 50);
    AuthSupport.givenBearer(admin).contentType(ContentType.JSON).body("{}").when()
        .patch("/api/events/{id}", id).then().statusCode(400);
    AuthSupport.givenBearer(admin).contentType(ContentType.JSON).body("{\"maxSeats\":-1}").when()
        .patch("/api/events/{id}", id).then().statusCode(400);
  }

  @Test @Story("Get Event by Name")
  void getEventByName_returns200() {
    String name = unique("Named Event");
    createEventId(adminToken(), name, 50);
    given().filter(new AllureRestAssured()).queryParam("name", name).when().get("/api/events").then()
        .statusCode(200).body("[0].id", notNullValue()).body("[0].name", equalTo(name))
        .body("size()", equalTo(1));
  }

  @Test @Story("Get Event by ID")
  void getEventById_returns200() {
    Integer id = createEventId(adminToken(), unique("ID Event"), 50);
    given().filter(new AllureRestAssured()).when().get("/api/events/{id}", id).then()
        .statusCode(200).body("id", equalTo(id)).body("maxSeats", equalTo(50));
  }

  @Test @Story("Get Event - Empty Result")
  void getEventByUnknownName_returnsEmpty() {
    given().filter(new AllureRestAssured()).queryParam("name", unique("Unknown")).when().get("/api/events")
        .then().statusCode(200).body("size()", equalTo(0));
  }

  @Test @Story("Delete Event")
  void deleteEvent_returns204() {
    String admin = adminToken();
    Integer id = createEventId(admin, unique("Delete Event"), 999);
    AuthSupport.givenBearer(admin).filter(new AllureRestAssured()).when().delete("/api/events/{id}", id)
        .then().statusCode(204);
    given().filter(new AllureRestAssured()).when().get("/api/events/{id}", id).then().statusCode(404);
  }

  @Test @Story("Get Event by ID - Not Found")
  void getEventById_returns404WhenMissing() {
    given().filter(new AllureRestAssured()).when().get("/api/events/{id}", Integer.MAX_VALUE)
        .then().statusCode(404);
  }

  @Test @Story("Search Events with Pagination")
  void searchEvents_returnsPagedResult() {
    String prefix = unique("Search");
    String alphaOne = prefix + " Alpha One";
    String alphaTwo = prefix + " Alpha Two";
    createEventId(adminToken(), alphaOne, 10);
    createEventId(adminToken(), prefix + " Beta", 20);
    createEventId(adminToken(), alphaTwo, 30);
    given().filter(new AllureRestAssured()).queryParam("page", 0).queryParam("size", 1)
        .queryParam("q", prefix + " Alpha").when().get("/api/events").then().statusCode(200)
        .body("content.size()", equalTo(1)).body("totalElements", equalTo(2))
        .body("totalPages", equalTo(2)).body("number", equalTo(0));
  }

  @Test @Story("Create Bulk Events")
  void createEventsBulk_returnsList() {
    String admin = adminToken();
    String prefix = unique("Bulk");
    List<Map<String, Object>> payload = List.of(
        Map.of("name", prefix + " One", "maxSeats", 10),
        Map.of("name", prefix + " Two", "maxSeats", 20),
        Map.of("name", prefix + " Three", "maxSeats", 30));
    List<?> created = AuthSupport.givenBearer(admin).contentType(ContentType.JSON).body(payload)
        .when().post("/api/events/bulk").then().statusCode(201).body("size()", equalTo(3))
        .body("name", hasItem(prefix + " One"))
        .extract().jsonPath().getList("");
    @SuppressWarnings("unchecked")
    Map<String, Object> third = (Map<String, Object>) created.get(2);
    Integer deletedId = ((Number) third.get("id")).intValue();
    AuthSupport.givenBearer(admin).when().delete("/api/events/{id}", deletedId).then().statusCode(204);
    given().queryParam("page", 0).queryParam("size", 100).queryParam("q", prefix)
        .when().get("/api/events").then().statusCode(200)
        .body("content.size()", equalTo(2))
        .body("content.name", hasItem(prefix + " One"))
        .body("content.name", not(hasItem(prefix + " Three")));
  }

  @Test @Story("Create Invalid Bulk Events")
  void createInvalidEventsBulk_returns400() {
    AuthSupport.givenBearer(adminToken()).contentType(ContentType.JSON)
        .body(List.of(Map.of("name", unique("Invalid Bulk"), "maxSeats", -1)))
        .when().post("/api/events/bulk").then().statusCode(400);
  }

  @Test @Story("Create Duplicated Bulk Events")
  void createDuplicatedEventsBulk_returns400() {
    String name = unique("Duplicate Bulk");
    AuthSupport.givenBearer(adminToken()).contentType(ContentType.JSON)
        .body(List.of(Map.of("name", name, "maxSeats", 1), Map.of("name", name, "maxSeats", 2)))
        .when().post("/api/events/bulk").then().statusCode(400);
  }
}
