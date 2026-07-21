package com.fettqa.events.registration;

import com.fettqa.events.event.Event;
import com.fettqa.events.event.EventNotFoundException;
import com.fettqa.events.event.EventRepository;
import com.fettqa.events.registration.dto.EventRegistrationRequest;
import com.fettqa.events.registration.dto.EventRegistrationResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

  private final RegistrationRepository registrationRepository;
  private final EventRepository eventRepository;

  public RegistrationService(RegistrationRepository registrationRepository,
      EventRepository eventRepository) {
    this.registrationRepository = registrationRepository;
    this.eventRepository = eventRepository;
  }

  @Transactional
  public EventRegistrationResponse register(Long eventId, EventRegistrationRequest request) {
    // Lock event row for the whole transaction so concurrent requests cannot
    // both pass the seat-limit check (check-then-act race).
    Event event = eventRepository.findByIdForUpdate(eventId)
        .orElseThrow(() -> new EventNotFoundException("event with id: " + eventId + " not found"));

    if (registrationRepository.existsByEventIdAndEmailIgnoreCase(eventId, request.email())) {
      throw new RegistrationConflictException(
          "email " + request.email() + " is already registered for event " + event.getName());
    }

    if (registrationRepository.countByEventId(eventId) >= event.getMaxSeats()) {
      throw new RegistrationConflictException(
          "event " + event.getName() + " has reached its maximum capacity");
    }

    return EventRegistrationResponse.from(
        registrationRepository.save(new Registration(event, request.fullName(), request.email())));
  }

  public List<EventRegistrationResponse> getRegistrations(Long eventId) {
    if (!eventRepository.existsById(eventId)) {
      throw new EventNotFoundException("event with id: " + eventId + " not found");
    }
    return registrationRepository.findByEventId(eventId).stream()
        .map(EventRegistrationResponse::from)
        .toList();
  }

  public Page<EventRegistrationResponse> searchRegistrations(
      Long eventId, String query, Pageable pageable) {
    if (!eventRepository.existsById(eventId)) {
      throw new EventNotFoundException("event with id: " + eventId + " not found");
    }
    Page<Registration> page;
    if (query == null || query.isBlank()) {
      page = registrationRepository.findByEventId(eventId, pageable);
    } else {
      page = registrationRepository.findByEventIdAndFullNameContainingIgnoreCase(
          eventId, query.trim(), pageable);
    }
    return page.map(EventRegistrationResponse::from);
  }

  public long countByEventId(Long eventId) {
    return registrationRepository.countByEventId(eventId);
  }
}
