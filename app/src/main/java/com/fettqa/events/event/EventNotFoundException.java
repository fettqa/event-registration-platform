package com.fettqa.events.event;

public class EventNotFoundException extends RuntimeException {
  public EventNotFoundException(Long eventId) {
    super("Event not found: " + eventId);
  }
}
