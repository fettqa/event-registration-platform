package com.fettqa.events.event.rest;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;

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

@Epic("Admin Panel")
@Feature("Users")
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDataCleaner.class)
class AdminUserControllerApiTest {

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
  @Story("Admin can list users and their roles, excluding the ADMIN role")
  void listUsers_asAdmin_excludesAdminRole() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    AuthTestSupport.registerUser(port, "Panel User", "panel_" + suffix + "@example.com", "secret12");

    AuthTestSupport.givenAdmin(port)
        .filter(new AllureRestAssured())
        .when()
        .get("/api/admin/users")
        .then()
        .statusCode(200)
        .body("email", hasItem("panel_" + suffix + "@example.com"))
        .body("role", not(hasItem("ADMIN")));
  }

  @Test
  @Story("Admin can update a user's role to SUPER_USER")
  void updateRole_asAdmin_setsSuperUser() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    String email = "promote_" + suffix + "@example.com";
    AuthTestSupport.registerUser(port, "Promote Me", email, "secret12");

    Integer userId = AuthTestSupport.givenAdmin(port)
        .filter(new AllureRestAssured())
        .when()
        .get("/api/admin/users")
        .then()
        .statusCode(200)
        .extract()
        .jsonPath()
        .getList("", java.util.Map.class)
        .stream()
        .filter(row -> email.equals(row.get("email")))
        .map(row -> (Integer) row.get("id"))
        .findFirst()
        .orElseThrow();

    AuthTestSupport.givenAdmin(port)
        .contentType(ContentType.JSON)
        .body("{\"role\":\"SUPER_USER\"}")
        .filter(new AllureRestAssured())
        .when()
        .put("/api/admin/users/{id}/role", userId)
        .then()
        .statusCode(200)
        .body("role", equalTo("SUPER_USER"));
  }

  @Test
  @Story("Non-admin user cannot list users and receives 403")
  void listUsers_asUser_returns403() {
    String suffix = UUID.randomUUID().toString().substring(0, 8);
    String token = AuthTestSupport.registerUser(
        port, "No Admin", "noadmin_" + suffix + "@example.com", "secret12");

    AuthTestSupport.givenBearer(port, token)
        .filter(new AllureRestAssured())
        .when()
        .get("/api/admin/users")
        .then()
        .statusCode(403);
  }
}
