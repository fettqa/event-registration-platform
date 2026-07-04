package com.fettqa.events.event.dto;

import com.fettqa.events.event.Event;
import java.time.OffsetDateTime;

public record EventResponse(
    Long id,
    String name,
    Integer maxSeats,
    OffsetDateTime createdAt
) {
  public static EventResponse from(Event event) {
    return new EventResponse(
        event.getId(),
        event.getName(),
        event.getMaxSeats(),
        event.getCreatedAt()
    );
  }
}