package com.fettqa.events.messaging;

import com.fettqa.events.messaging.dto.RegistrationCreatedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Profile("kafka")
@Component
public class RegistrationCreatedListener {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegistrationCreatedListener.class);

    @KafkaListener(topics = "${app.kafka.topics.registration-created}")
    public void onMessage(RegistrationCreatedEvent event) {
        log.info("Notify: {} registered to event {}", event.email(), event.eventId());
    }
}