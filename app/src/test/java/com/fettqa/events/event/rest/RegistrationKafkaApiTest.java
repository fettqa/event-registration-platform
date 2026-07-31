package com.fettqa.events.event.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.fettqa.events.messaging.dto.RegistrationCreatedEvent;
import com.fettqa.events.utils.AuthTestSupport;
import com.fettqa.events.utils.TestDataCleaner;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

@Epic("Event Registration")
@Feature("Registration Kafka")
@ActiveProfiles({"test", "kafka"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
    partitions = 1,
    topics = "registration.created",
    bootstrapServersProperty = "spring.kafka.bootstrap-servers")
@Import(TestDataCleaner.class)
class RegistrationKafkaApiTest {

  private static final String TOPIC = "registration.created";

  @LocalServerPort
  int port;

  @Autowired
  TestDataCleaner testDataCleaner;

  @Autowired
  ConsumerFactory<String, RegistrationCreatedEvent> consumerFactory;

  private String adminToken;

  @BeforeEach
  void setUp() {
    testDataCleaner.cleanAndResetIds();
    RestAssured.port = port;
    RestAssured.basePath = "";
    adminToken = AuthTestSupport.adminToken(port);
  }

  @Test
  @Story("Register for Event publishes Kafka message")
  void registration_publishesRegistrationCreatedEvent() {
    String eventName = "Kafka QA Conf " + UUID.randomUUID().toString().substring(0, 8);
    Integer eventId = given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + adminToken)
        .contentType(ContentType.JSON)
        .body("{\"name\":\"" + eventName + "\",\"maxSeats\":10}")
        .when()
        .post("/api/events")
        .then()
        .statusCode(201)
        .extract().path("id");

    String email = "kafka_user_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    String token = AuthTestSupport.registerUser(port, "Kafka User", email, "secret12");

    given()
        .filter(new AllureRestAssured())
        .header("Authorization", "Bearer " + token)
        .when()
        .post("/api/events/{eventId}/registrations", eventId)
        .then()
        .statusCode(201)
        .body("eventId", equalTo(eventId))
        .body("email", equalTo(email))
        .body("fullName", equalTo("Kafka User"));

    try (var consumer = consumerFactory.createConsumer("registration-kafka-api-test", null)) {
      consumer.subscribe(List.of(TOPIC));
      var record = KafkaTestUtils.getSingleRecord(consumer, TOPIC, Duration.ofSeconds(10));

      assertThat(record.value().eventId()).isEqualTo(eventId.longValue());
      assertThat(record.value().eventName()).isEqualTo(eventName);
      assertThat(record.value().email()).isEqualTo(email);
      assertThat(record.value().fullName()).isEqualTo("Kafka User");
      assertThat(record.value().registrationId()).isNotNull();
    }
  }
}
