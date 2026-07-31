package com.fettqa.events.registration;

import com.fettqa.events.auth.Role;
import com.fettqa.events.auth.SecurityUtils;
import com.fettqa.events.auth.User;
import com.fettqa.events.event.Event;
import com.fettqa.events.event.EventNotFoundException;
import com.fettqa.events.event.EventRepository;
import com.fettqa.events.messaging.dto.RegistrationCreatedEvent;
import com.fettqa.events.messaging.RegistrationEventPublisher;
import com.fettqa.events.registration.dto.EventRegistrationResponse;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistrationService {

  private final RegistrationRepository registrationRepository;
  private final EventRepository eventRepository;
  private final Optional<RegistrationEventPublisher> publisher;

  public RegistrationService(RegistrationRepository registrationRepository,
                             EventRepository eventRepository, Optional<RegistrationEventPublisher> publisher) {
    this.registrationRepository = registrationRepository;
    this.eventRepository = eventRepository;
    this.publisher = publisher;
  }

  @Transactional
  public EventRegistrationResponse registerCurrentUser(Long eventId) {
    User user = SecurityUtils.requireCurrentUser();
    return register(eventId, user.getFullName(), user.getEmail());
  }

  @Transactional
  public EventRegistrationResponse register(Long eventId, String fullName, String email) {
    Event event = eventRepository.findByIdForUpdate(eventId)
        .orElseThrow(() -> new EventNotFoundException("event with id: " + eventId + " not found"));

    if (registrationRepository.existsByEventIdAndEmailIgnoreCase(eventId, email)) {
      throw new RegistrationConflictException(
          "email " + email + " is already registered for event " + event.getName());
    }

    if (registrationRepository.countByEventId(eventId) >= event.getMaxSeats()) {
      throw new RegistrationConflictException(
          "event " + event.getName() + " has reached its maximum capacity");
    }

    Registration saved = registrationRepository.save(new Registration(event, fullName, email));
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        publisher.ifPresent(p -> p.publish(RegistrationCreatedEvent.from(saved)));
      }
    });
    return EventRegistrationResponse.from(saved);
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

  @Transactional
  public void deleteRegistration(Long eventId, Long registrationId) {
    User current = SecurityUtils.requireCurrentUser();
    Registration registration = registrationRepository.findById(registrationId)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.NOT_FOUND, "registration with id: " + registrationId + " not found"));

    Event event = registration.getEvent();
    if (event == null || !event.getId().equals(eventId)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND,
          "registration with id: " + registrationId + " not found for event " + eventId);
    }

    if (!canDeleteRegistration(current, event, registration)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN,
          "not allowed to delete this registration");
    }
    registrationRepository.delete(registration);
  }

  private boolean canDeleteRegistration(User user, Event event, Registration registration) {
    if (user.getRole() == Role.ADMIN) {
      return true;
    }
    if (user.getRole() == Role.SUPER_USER
        && event.getCreatedBy() != null
        && event.getCreatedBy().getId().equals(user.getId())) {
      return true;
    }
    return registration.getEmail() != null
        && registration.getEmail().equalsIgnoreCase(user.getEmail());
  }
}
