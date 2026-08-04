package com.fettqa.events.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.fettqa.events.messaging.dto.RegistrationCreatedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class RegistrationEmailServiceTest {

  @Mock JavaMailSender mailSender;

  @Test
  void sendRegistrationConfirmation_setsToSubjectAndBody() {
    var service = new RegistrationEmailService(mailSender, "noreply@events.local");
    var event =
        new RegistrationCreatedEvent(10L, 3L, "QA Conf", "user@example.com", "Ada", "2026-01-01T00:00");

    service.sendRegistrationConfirmation(event);

    ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(mailSender).send(captor.capture());
    SimpleMailMessage msg = captor.getValue();
    assertThat(msg.getFrom()).isEqualTo("noreply@events.local");
    assertThat(msg.getTo()).containsExactly("user@example.com");
    assertThat(msg.getSubject()).contains("QA Conf");
    assertThat(msg.getText()).contains("Ada").contains("QA Conf");
  }
}
