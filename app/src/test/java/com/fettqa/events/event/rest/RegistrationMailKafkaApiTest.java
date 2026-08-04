package com.fettqa.events.event.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fettqa.events.utils.AuthTestSupport;
import com.fettqa.events.utils.TestDataCleaner;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.mail.internet.MimeMessage;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@Epic("Event Registration")
@Feature("Registration Mail")
@ActiveProfiles({"test", "kafka", "mail"})
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.mail.host=127.0.0.1",
      "spring.mail.port=3025",
      "spring.mail.properties.mail.smtp.auth=false",
      "spring.mail.properties.mail.smtp.starttls.enable=false",
      "app.mail.from=noreply@events.local"
    })
@EmbeddedKafka(
    partitions = 1,
    topics = "registration.created",
    bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@Import(TestDataCleaner.class)
class RegistrationMailKafkaApiTest {

  /** GreenMail SMTP port matches {@code spring.mail.port=3025} (ServerSetupTest.SMTP). */
  @RegisterExtension
  static GreenMailExtension greenMail =
      new GreenMailExtension(ServerSetupTest.SMTP)
          .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());

  @LocalServerPort
  int port;

  @Autowired
  TestDataCleaner testDataCleaner;

  private String adminToken;

  @BeforeEach
  void setUp() throws Exception {
    greenMail.purgeEmailFromAllMailboxes();
    testDataCleaner.cleanAndResetIds();
    RestAssured.port = port;
    RestAssured.basePath = "";
    adminToken = AuthTestSupport.adminToken(port);
  }

  @Test
  @Story("Register for Event sends confirmation email via Kafka listener")
  void registration_sendsConfirmationEmail() throws Exception {
    String eventName = "Mail QA Conf " + UUID.randomUUID().toString().substring(0, 8);
    Integer eventId =
        given()
            .filter(new AllureRestAssured())
            .header("Authorization", "Bearer " + adminToken)
            .contentType(ContentType.JSON)
            .body("{\"name\":\"" + eventName + "\",\"maxSeats\":10}")
            .when()
            .post("/api/events")
            .then()
            .statusCode(201)
            .extract()
            .path("id");

    String email = "mail_user_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    String token = AuthTestSupport.registerUser(port, "Mail User", email, "secret12");

    given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + token)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(201)
        .body("email", equalTo(email));

    assertThat(greenMail.waitForIncomingEmail(10_000, 1)).isTrue();
    MimeMessage[] messages = greenMail.getReceivedMessages();
    assertThat(messages).hasSize(1);
    assertThat(messages[0].getAllRecipients()[0].toString()).contains(email);
    assertThat(messages[0].getSubject()).contains(eventName);
    assertThat((String) messages[0].getContent()).contains("Mail User");
  }
}
