package com.fettqa.events.api;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fettqa.events.api.support.AuthSupport;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

@Epic("Event Registration")
@Feature("Register / List Operations")
class RegistrationApiTest extends ApiTestBase {

  @Test @Story("Register for Event")
  void registration_returns201() {
    Integer eventId = createEventId(adminToken(), unique("Registration"), 10);
    String email = uniqueEmail("ivan");
    String token = AuthSupport.registerUser("Ivan", email, "secret12");
    AuthSupport.givenBearer(token).filter(new AllureRestAssured()).when()
        .post("/api/events/{id}/registrations", eventId).then().statusCode(201)
        .body("eventId", equalTo(eventId)).body("email", equalTo(email)).body("fullName", equalTo("Ivan"));
  }

  @Test @Story("Register for Event without Token")
  void registration_withoutToken_returns401or403() {
    Integer eventId = createEventId(adminToken(), unique("Unauthorized Registration"), 10);
    int status = given().filter(new AllureRestAssured()).when()
        .post("/api/events/{id}/registrations", eventId).then().extract().statusCode();
    assertThat(status).isIn(401, 403);
  }

  @Test @Story("Get Registrations for Event")
  void get_registration_returns200() {
    Integer eventId = createEventId(adminToken(), unique("Listed Registration"), 10);
    String email = uniqueEmail("listed");
    String token = AuthSupport.registerUser("Ivan", email, "secret12");
    register(token, eventId);
    given().filter(new AllureRestAssured()).when().get("/api/events/{id}/registrations", eventId).then()
        .statusCode(200).body("size()", equalTo(1)).body("[0].email", equalTo(email));
  }

  @Test @Story("Get Registrations for Non-Existent Event")
  void get_registration_returns404() {
    given().filter(new AllureRestAssured()).when().get("/api/events/{id}/registrations", Integer.MAX_VALUE)
        .then().statusCode(404);
  }

  @Test @Story("Get Registrations for Event with No Registrations")
  void get_registration_returns_empty_list() {
    Integer eventId = createEventId(adminToken(), unique("Empty Registration"), 10);
    given().filter(new AllureRestAssured()).when().get("/api/events/{id}/registrations", eventId).then()
        .statusCode(200).body("size()", equalTo(0));
  }

  @Test @Story("Search Registrations with Pagination")
  void search_registrations_returnsPagedResult() {
    Integer eventId = createEventId(adminToken(), unique("Registration Search"), 10);
    String prefix = unique("Registrant");
    register(AuthSupport.registerUser(prefix + " Alice One", uniqueEmail("alice-one"), "secret12"), eventId);
    register(AuthSupport.registerUser(prefix + " Bob", uniqueEmail("bob"), "secret12"), eventId);
    register(AuthSupport.registerUser(prefix + " Alice Two", uniqueEmail("alice-two"), "secret12"), eventId);
    given().filter(new AllureRestAssured()).queryParam("page", 0).queryParam("size", 1)
        .queryParam("q", prefix + " Alice").when().get("/api/events/{id}/registrations", eventId).then()
        .statusCode(200).body("content.size()", equalTo(1)).body("totalElements", equalTo(2))
        .body("totalPages", equalTo(2)).body("number", equalTo(0));
  }

  @Test @Story("Register for Event when Event is Full")
  void registration_returns409_whenEventIsFull() {
    Integer eventId = createEventId(adminToken(), unique("Full Event"), 1);
    register(AuthSupport.registerUser("First", uniqueEmail("first"), "secret12"), eventId);
    AuthSupport.givenBearer(AuthSupport.registerUser("Second", uniqueEmail("second"), "secret12")).when()
        .post("/api/events/{id}/registrations", eventId).then().statusCode(409);
  }

  @Test @Story("Register for Event with Already Registered Email")
  void registration_returns409_whenEmailAlreadyRegistered() {
    Integer eventId = createEventId(adminToken(), unique("Duplicate Registration"), 10);
    String token = AuthSupport.registerUser("Ivan", uniqueEmail("duplicate"), "secret12");
    register(token, eventId);
    AuthSupport.givenBearer(token).when().post("/api/events/{id}/registrations", eventId).then().statusCode(409);
  }

  @Test @Story("Concurrent Registration Respects Max Seats")
  void registration_concurrent_respectsMaxSeats() throws Exception {
    Integer eventId = createEventId(adminToken(), unique("Concurrent Registration"), 1);
    String first = AuthSupport.registerUser("First", uniqueEmail("concurrent-first"), "secret12");
    String second = AuthSupport.registerUser("Second", uniqueEmail("concurrent-second"), "secret12");
    CountDownLatch ready = new CountDownLatch(2);
    CountDownLatch start = new CountDownLatch(1);
    ConcurrentLinkedQueue<Integer> statuses = new ConcurrentLinkedQueue<>();
    Consumer<String> register = token -> {
      ready.countDown();
      try {
        start.await();
      } catch (InterruptedException ex) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(ex);
      }
      statuses.add(AuthSupport.givenBearer(token).when()
          .post("/api/events/{id}/registrations", eventId).then().extract().statusCode());
    };
    Thread firstThread = new Thread(() -> register.accept(first));
    Thread secondThread = new Thread(() -> register.accept(second));
    firstThread.start(); secondThread.start(); ready.await(); start.countDown();
    firstThread.join(); secondThread.join();
    assertThat(statuses).containsExactlyInAnyOrder(201, 409);
  }

  private Integer register(String token, Integer eventId) {
    return AuthSupport.givenBearer(token).filter(new AllureRestAssured()).when()
        .post("/api/events/{id}/registrations", eventId).then().statusCode(201).extract().path("id");
  }
}
