package com.fettqa.events.event;

import com.fettqa.events.event.dto.CreateEventRequest;
import com.fettqa.events.event.dto.EventResponse;
import com.fettqa.events.event.dto.UpdateEventRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EventService {

  private final EventRepository eventRepository;

  public EventService(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  @Transactional
  public List<EventResponse> create(List<CreateEventRequest> request) {
    List<Event> events = request.stream()
        .map(r -> new Event(r.name(), r.maxSeats()))
        .toList();
    eventRepository.saveAll(events);
    return events.stream().map(EventResponse::from).toList();
  }

  @Transactional
  public EventResponse create(CreateEventRequest request) {
    Event event = new Event(request.name(), request.maxSeats());
    Event saved = eventRepository.save(event);
    return EventResponse.from(saved);
  }

  public EventResponse getById(Long id) {
    Event event = eventRepository.findById(id)
        .orElseThrow(() -> new EventNotFoundException("event with id: " + id + " not found"));
    return EventResponse.from(event);
  }

  @Transactional
  public EventResponse updateById(Long id, UpdateEventRequest request) {
    if (request.name() == null && request.maxSeats() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nothing to update");
    }
    Event event = eventRepository.findById(id)
        .orElseThrow(() -> new EventNotFoundException("event with id: " + id + " not found"));
    if (request.maxSeats() != null) {
      event.setMaxSeats(request.maxSeats());
    }
    if (request.name() != null) {
      event.setName(request.name());
    }
    return EventResponse.from(event);
  }

  public List<EventResponse> getByName(String name) {
    List<Event> events = eventRepository.findByName(name);
    return events.stream().map(EventResponse::from).toList();
  }

  public List<EventResponse> getAll() {
    return eventRepository.findAll().stream()
        .map(EventResponse::from)
        .toList();
  }

  public void delete(Long id) {
    Event event = eventRepository.findById(id)
        .orElseThrow(() -> new EventNotFoundException("event with id: " + id + " not found"));
    eventRepository.delete(event);
  }
}