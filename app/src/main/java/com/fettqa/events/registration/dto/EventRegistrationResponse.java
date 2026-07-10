package com.fettqa.events.registration.dto;

import com.fettqa.events.registration.Registration;
import java.time.OffsetDateTime;

public record EventRegistrationResponse(Long id, Long eventId, String email, String fullName,
                                        OffsetDateTime createdAt) {

  public static EventRegistrationResponse from(Registration registration) {
    return new EventRegistrationResponse(
        registration.getId(),
        registration.getEvent().getId(),
        registration.getEmail(),
        registration.getFullName(),
        registration.getCreatedAt()
    );

  }
}
