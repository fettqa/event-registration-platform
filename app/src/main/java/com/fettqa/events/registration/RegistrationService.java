package com.fettqa.events.registration;

import com.fettqa.events.event.Event;
import com.fettqa.events.event.EventNotFoundException;
import com.fettqa.events.event.EventRepository;
import com.fettqa.events.registration.dto.EventRegistrationRequest;
import com.fettqa.events.registration.dto.EventRegistrationResponse;
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
    Event event = eventRepository.findById(eventId)
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

}
