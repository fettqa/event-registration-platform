package com.fettqa.events.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import com.fettqa.events.api.support.AuthSupport;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
import org.junit.jupiter.api.Test;

@Epic("Permissions")
@Feature("Delete event/registration")
class DeletePermissionsApiTest extends ApiTestBase {

  @Test @Story("Admin can delete any event")
  void admin_canDeleteAnyEvent() {
    String admin = adminToken();
    Integer id = createEventId(admin, unique("Admin Delete"), 5);
    AuthSupport.givenBearer(admin).filter(new AllureRestAssured()).when().delete("/api/events/{id}", id)
        .then().statusCode(204);
  }

  @Test @Story("User cannot delete events")
  void user_cannotDeleteEvent() {
    String admin = adminToken();
    Integer id = createEventId(admin, unique("No Delete"), 5);
    String user = AuthSupport.registerUser("No Delete", uniqueEmail("nodelete"), "secret12");
    AuthSupport.givenBearer(user).filter(new AllureRestAssured()).when().delete("/api/events/{id}", id)
        .then().statusCode(403);
  }

  @Test @Story("User can delete own registration")
  void user_canDeleteOwnRegistration() {
    String admin = adminToken();
    Integer eventId = createEventId(admin, unique("Own Registration"), 5);
    String user = AuthSupport.registerUser("Own Registration", uniqueEmail("ownreg"), "secret12");
    Integer registrationId = register(user, eventId);
    AuthSupport.givenBearer(user).filter(new AllureRestAssured())
        .when().delete("/api/events/{eventId}/registrations/{registrationId}", eventId, registrationId)
        .then().statusCode(204);
    given().filter(new AllureRestAssured()).when().get("/api/events/{id}/registrations", eventId)
        .then().statusCode(200).body("size()", equalTo(0));
  }

  @Test @Story("User cannot delete someone else's registration")
  void user_cannotDeleteSomeoneElsesRegistration() {
    String admin = adminToken();
    Integer eventId = createEventId(admin, unique("Foreign Registration"), 5);
    String owner = AuthSupport.registerUser("Owner", uniqueEmail("owner"), "secret12");
    String other = AuthSupport.registerUser("Other", uniqueEmail("other"), "secret12");
    Integer registrationId = register(owner, eventId);
    AuthSupport.givenBearer(other).filter(new AllureRestAssured())
        .when().delete("/api/events/{eventId}/registrations/{registrationId}", eventId, registrationId)
        .then().statusCode(403);
  }

  @Test @Story("Admin can delete any registration")
  void admin_canDeleteAnyRegistration() {
    String admin = adminToken();
    Integer eventId = createEventId(admin, unique("Admin Registration Delete"), 5);
    String user = AuthSupport.registerUser("Victim", uniqueEmail("victim"), "secret12");
    Integer registrationId = register(user, eventId);
    AuthSupport.givenBearer(admin).filter(new AllureRestAssured())
        .when().delete("/api/events/{eventId}/registrations/{registrationId}", eventId, registrationId)
        .then().statusCode(204);
  }

  private Integer register(String token, Integer eventId) {
    return AuthSupport.givenBearer(token).filter(new AllureRestAssured()).when()
        .post("/api/events/{id}/registrations", eventId).then().statusCode(201).extract().path("id");
  }
}
