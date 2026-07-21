package com.fettqa.events.event;

import com.fettqa.events.event.dto.CreateEventRequest;
import com.fettqa.events.event.dto.EventResponse;
import com.fettqa.events.event.dto.UpdateEventRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Set<String> uniqNames = events.stream().map(Event::getName).collect(Collectors.toSet());
    if (uniqNames.size() != events.size()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate event names in request");
    }
    List<String> existingEvents = new ArrayList<>();
    events.forEach(event -> {
      if (!eventRepository.findByName(event.getName()).isEmpty()) {
        existingEvents.add(event.getName());
      }
    });
    if (!existingEvents.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "events with names: " + String.join(", ", existingEvents) + " already exist");
    }
    eventRepository.saveAll(events);
    return events.stream().map(EventResponse::from).toList();
  }

  @Transactional
  public EventResponse create(CreateEventRequest request) {
    Event event = new Event(request.name(), request.maxSeats());
    if (request.name().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank");
    }
    if (!eventRepository.findByName(request.name()).isEmpty()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT,
          "event with name: " + request.name() + " already exists");
    }
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
    if (request.name() != null && request.name().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name must not be blank");
    }
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

  public Page<EventResponse> search(String query, Pageable pageable) {
    Page<Event> page;
    if (query == null || query.isBlank()) {
      page = eventRepository.findAll(pageable);
    } else {
      page = eventRepository.findByNameContainingIgnoreCase(query.trim(), pageable);
    }
    return page.map(EventResponse::from);
  }

  @Transactional
  public void delete(Long id) {
    Event event = eventRepository.findById(id)
        .orElseThrow(() -> new EventNotFoundException("event with id: " + id + " not found"));
    eventRepository.delete(event);
  }
}