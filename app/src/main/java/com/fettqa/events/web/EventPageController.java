package com.fettqa.events.web;

import com.fettqa.events.event.EventService;
import com.fettqa.events.event.dto.EventResponse;
import com.fettqa.events.registration.RegistrationService;
import com.fettqa.events.registration.dto.EventRegistrationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EventPageController {

  private static final int PAGE_SIZE = 100;

  private final EventService eventService;
  private final RegistrationService registrationService;

  public EventPageController(EventService eventService,
      RegistrationService registrationService) {
    this.eventService = eventService;
    this.registrationService = registrationService;
  }

  @GetMapping("/")
  public String index(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(required = false) String q,
      Model model) {
    Page<EventResponse> events = eventService.search(
        q, PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));
    model.addAttribute("events", events);
    model.addAttribute("q", q == null ? "" : q);
    return "events/list";
  }

  @GetMapping("/events/new")
  public String newEvent() {
    return "events/new";
  }

  @GetMapping("/events/{id}")
  public String eventDetails(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Boolean registered,
      Model model) {
    EventResponse event = eventService.getById(id);
    long registeredCount = registrationService.countByEventId(id);
    int seatsLeft = Math.max(0, event.maxSeats() - (int) registeredCount);

    Page<EventRegistrationResponse> registrations = registrationService.searchRegistrations(
        id, q, PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));

    model.addAttribute("event", event);
    model.addAttribute("seatsLeft", seatsLeft);
    model.addAttribute("registeredCount", registeredCount);
    model.addAttribute("registrations", registrations);
    model.addAttribute("q", q == null ? "" : q);
    model.addAttribute("justRegistered", Boolean.TRUE.equals(registered));
    return "events/details";
  }
}
