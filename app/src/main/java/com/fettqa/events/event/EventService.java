package com.fettqa.events.event;

import com.fettqa.events.event.dto.CreateEventRequest;
import com.fettqa.events.event.dto.EventResponse;
import java.util.List;
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

  public EventResponse getById(Long id) {
    Event event = eventRepository.findById(id)
        .orElseThrow(() -> new EventNotFoundException(id));
    return EventResponse.from(event);
  }

  public List<EventResponse> getAll() {
    return eventRepository.findAll().stream()
        .map(EventResponse::from)
        .toList();
  }
}