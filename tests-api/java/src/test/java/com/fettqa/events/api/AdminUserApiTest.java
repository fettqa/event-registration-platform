package com.fettqa.events.api;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

import com.fettqa.events.api.support.AuthSupport;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.http.ContentType;
import java.util.Map;
import org.junit.jupiter.api.Test;

@Epic("Admin Panel")
@Feature("Users")
class AdminUserApiTest extends ApiTestBase {

  @Test
  @Story("Admin can list users and their roles, excluding the ADMIN role")
  void listUsers_asAdmin_excludesAdminRole() {
    String email = uniqueEmail("panel");
    AuthSupport.registerUser("Panel User", email, "secret12");
    AuthSupport.givenAdmin().filter(new AllureRestAssured()).when().get("/api/admin/users").then()
        .statusCode(200).body("email", hasItem(email)).body("role", not(hasItem("ADMIN")));
  }

  @Test
  @Story("Admin can update a user's role to SUPER_USER")
  void updateRole_asAdmin_setsSuperUser() {
    String email = uniqueEmail("promote");
    AuthSupport.registerUser("Promote Me", email, "secret12");
    Integer userId = AuthSupport.givenAdmin().when().get("/api/admin/users").then().statusCode(200)
        .extract().jsonPath().getList("", Map.class).stream()
        .filter(row -> email.equals(row.get("email")))
        .map(row -> ((Number) row.get("id")).intValue()).findFirst().orElseThrow();

    AuthSupport.givenAdmin().filter(new AllureRestAssured()).contentType(ContentType.JSON)
        .body("{\"role\":\"SUPER_USER\"}").when().put("/api/admin/users/{id}/role", userId)
        .then().statusCode(200).body("role", equalTo("SUPER_USER"));
  }

  @Test
  @Story("Non-admin user cannot list users and receives 403")
  void listUsers_asUser_returns403() {
    String token = AuthSupport.registerUser("No Admin", uniqueEmail("noadmin"), "secret12");
    AuthSupport.givenBearer(token).filter(new AllureRestAssured()).when().get("/api/admin/users")
        .then().statusCode(403);
  }
}
