package com.fettqa.events.event;

public class EventNotFoundException extends RuntimeException {
  public EventNotFoundException(Long eventId) {
    super("Event not found: " + eventId);
  }
  public EventNotFoundException(String name) {
    super("Event not found: " + name);
  }
}
