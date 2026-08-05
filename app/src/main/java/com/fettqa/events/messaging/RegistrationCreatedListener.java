package com.fettqa.events.messaging;

import com.fettqa.events.messaging.dto.RegistrationCreatedEvent;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Profile("kafka")
@Component
public class RegistrationCreatedListener {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(RegistrationCreatedListener.class);

  private final Optional<RegistrationEmailService> emailService;

  public RegistrationCreatedListener(Optional<RegistrationEmailService> emailService) {
    this.emailService = emailService;
  }

  @KafkaListener(topics = "${app.kafka.topics.registration-created}")
  public void onMessage(RegistrationCreatedEvent event) {
    log.info("Notify: {} registered to event {}", event.email(), event.eventId());
    emailService.ifPresent(service -> service.sendRegistrationConfirmation(event));
  }
}
