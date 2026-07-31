package com.fettqa.events.messaging.dto;

import com.fettqa.events.registration.Registration;

public record RegistrationCreatedEvent(
        Long registrationId,
        Long eventId,
        String eventName,
        String email,
        String fullName,
        String createdAt
) {
    public static RegistrationCreatedEvent from(Registration saved) {
        return new RegistrationCreatedEvent(
                saved.getId(),
                saved.getEvent().getId(),
                saved.getEvent().getName(),
                saved.getEmail(),
                saved.getFullName(),
                saved.getCreatedAt().toString()
        );
    }
}