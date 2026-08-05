package com.fettqa.events.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fettqa.events.api.support.AuthSupport;
import com.fettqa.events.api.support.KafkaSupport;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import java.time.Duration;
import java.util.List;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("kafka")
@Epic("Event Registration")
@Feature("Registration Kafka")
class RegistrationKafkaApiTest extends ApiTestBase {

  private static final String TOPIC = "registration.created";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test @Story("Register for Event publishes Kafka message")
  void registration_publishesRegistrationCreatedEvent() throws Exception {
    Assumptions.assumeTrue(KafkaSupport.available(),
        () -> "Kafka is not available at " + KafkaSupport.bootstrapServers());
    String admin = adminToken();
    String eventName = unique("Kafka Event");
    Integer eventId = createEventId(admin, eventName, 10);
    String email = uniqueEmail("kafka-user");
    String token = AuthSupport.registerUser("Kafka User", email, "secret12");

    try (Consumer<String, String> consumer = KafkaSupport.newConsumer()) {
      consumer.subscribe(List.of(TOPIC));
      consumer.poll(Duration.ofSeconds(1));
      AuthSupport.givenBearer(token).when().post("/api/events/{id}/registrations", eventId).then()
          .statusCode(201);
      ConsumerRecord<String, String> record = null;
      for (ConsumerRecord<String, String> candidate :
          KafkaSupport.pollUntilRecord(consumer, Duration.ofSeconds(10)).records(TOPIC)) {
        if (MAPPER.readTree(candidate.value()).path("email").asText().equals(email)) {
          record = candidate;
          break;
        }
      }
      if (record == null) {
        throw new AssertionError("No Kafka event for " + email);
      }
      var message = MAPPER.readTree(record.value());
      assertThat(message.path("eventId").asLong()).isEqualTo(eventId.longValue());
      assertThat(message.path("eventName").asText()).isEqualTo(eventName);
      assertThat(message.path("email").asText()).isEqualTo(email);
      assertThat(message.path("fullName").asText()).isEqualTo("Kafka User");
      assertThat(message.path("registrationId").isNumber()).isTrue();
    }
  }
}
