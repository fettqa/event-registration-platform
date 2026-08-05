package com.fettqa.events.messaging;

import com.fettqa.events.messaging.dto.RegistrationCreatedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Profile("mail")
@Service
public class RegistrationEmailService {
  private static final org.slf4j.Logger log =
      org.slf4j.LoggerFactory.getLogger(RegistrationEmailService.class);

  private final JavaMailSender mailSender;
  private final String from;

  public RegistrationEmailService(
      JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
    this.mailSender = mailSender;
    this.from = from;
  }

  public void sendRegistrationConfirmation(RegistrationCreatedEvent event) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(event.email());
    message.setSubject("Registered: " + event.eventName());
    message.setText(
        """
        Hi %s,

        You are registered for "%s" (event id %d).
        Registration id: %d
        """
            .formatted(
                event.fullName(), event.eventName(), event.eventId(), event.registrationId()));
    mailSender.send(message);
    log.info("Sent registration email to {} for event {}", event.email(), event.eventId());
  }
}
