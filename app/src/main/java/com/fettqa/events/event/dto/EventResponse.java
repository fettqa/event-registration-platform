package com.fettqa.events.event.dto;

import com.fettqa.events.event.Event;
import java.time.OffsetDateTime;

public record EventResponse(
    Long id,
    String name,
    Integer maxSeats,
    Long createdById,
    String createdByEmail,
    OffsetDateTime createdAt
) {
  public static EventResponse from(Event event) {
    Long createdById = event.getCreatedBy() != null ? event.getCreatedBy().getId() : null;
    String createdByEmail = event.getCreatedBy() != null ? event.getCreatedBy().getEmail() : null;
    return new EventResponse(
        event.getId(),
        event.getName(),
        event.getMaxSeats(),
        createdById,
        createdByEmail,
        event.getCreatedAt()
    );
  }
}
