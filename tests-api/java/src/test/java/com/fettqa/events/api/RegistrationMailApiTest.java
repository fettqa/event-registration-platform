package com.fettqa.events.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fettqa.events.api.support.AuthSupport;
import com.fettqa.events.api.support.MailpitSupport;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.time.Duration;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("mail")
@Epic("Event Registration")
@Feature("Registration Mail")
class RegistrationMailApiTest extends ApiTestBase {

  @Test @Story("Register for Event sends confirmation email")
  void registration_sendsConfirmationEmail() throws Exception {
    Assumptions.assumeTrue(MailpitSupport.available(),
        () -> "Mailpit is not available at " + MailpitSupport.baseUrl());
    String eventName = unique("Mail Event");
    Integer eventId = createEventId(adminToken(), eventName, 10);
    String email = uniqueEmail("mail-user");
    String token = AuthSupport.registerUser("Mail User", email, "secret12");

    AuthSupport.givenBearer(token).when().post("/api/events/{id}/registrations", eventId).then()
        .statusCode(201);

    JsonNode message = MailpitSupport.waitForMessage(email, eventName, Duration.ofSeconds(10));
    assertThat(message.path("To").toString()).contains(email);
    assertThat(message.path("Subject").asText()).contains(eventName);
  }
}
