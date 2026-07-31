package com.fettqa.events.event.spring_boot;

import com.fettqa.events.messaging.RegistrationEventPublisher;
import com.fettqa.events.messaging.dto.RegistrationCreatedEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

@SpringBootTest
@ActiveProfiles({"test", "kafka"})
@EmbeddedKafka(
        partitions = 1,
        topics = "registration.created",
        bootstrapServersProperty = "spring.kafka.bootstrap-servers")
class RegistrationKafkaIT {

    @Autowired
    ConsumerFactory<String, RegistrationCreatedEvent> consumerFactory;
    @Autowired
    RegistrationEventPublisher publisher;

    @Test
    void publishesAndRead() {
        publisher.publish(new RegistrationCreatedEvent(
                1L,
                1L,
                "Test_event",
                "test@example.com",
                "Test",
                OffsetDateTime.now().toString()));

        try (var consumer = consumerFactory.createConsumer("test-group", null)) {
            consumer.subscribe(List.of("registration.created"));
            var record = KafkaTestUtils.getSingleRecord(
                    consumer, "registration.created", Duration.ofSeconds(5));
            Assertions.assertEquals("Test_event", record.value().eventName());
            Assertions.assertEquals("test@example.com", record.value().email());
            Assertions.assertEquals("Test", record.value().fullName());
        }
    }
}
