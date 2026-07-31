package com.fettqa.events.messaging;

import com.fettqa.events.messaging.dto.RegistrationCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Profile("kafka")
@Component
public class RegistrationEventPublisher {
    private final KafkaTemplate<String, RegistrationCreatedEvent> kafka;
    private final String topic;

    public RegistrationEventPublisher(KafkaTemplate<String, RegistrationCreatedEvent> kafka, @Value("${app.kafka.topics.registration-created}") String topic) {
        this.kafka = kafka;
        this.topic = topic;
    }


    public void publish(RegistrationCreatedEvent event) {
        kafka.send(topic, String.valueOf(event.eventId()), event);
    }
}