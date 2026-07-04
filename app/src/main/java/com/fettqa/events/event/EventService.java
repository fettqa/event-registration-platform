package com.fettqa.events.event;

import com.fettqa.events.event.dto.CreateEventRequest;
import com.fettqa.events.event.dto.EventResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

  private final EventRepository eventRepository;

  public EventService(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  @Transactional
  public EventResponse create(CreateEventRequest request) {
    Event event = new Event(request.name(), request.maxSeats());
    Event saved = eventRepository.save(event);
    return EventResponse.from(saved);
  }
}