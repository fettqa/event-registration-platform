package com.fettqa.events.web;

import com.fettqa.events.event.EventService;
import com.fettqa.events.event.dto.CreateEventRequest;
import com.fettqa.events.event.dto.EventResponse;
import com.fettqa.events.registration.RegistrationService;
import com.fettqa.events.registration.dto.EventRegistrationRequest;
import com.fettqa.events.registration.dto.EventRegistrationResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
  public String newEvent(Model model) {
    model.addAttribute("eventForm", new CreateEventRequest("", 10));
    return "events/new";
  }

  @PostMapping("/events")
  public String createEvent(@Valid @ModelAttribute("eventForm") CreateEventRequest form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "events/new";
    }
    try {
      EventResponse created = eventService.create(form);
      return "redirect:/events/" + created.id();
    } catch (ResponseStatusException ex) {
      model.addAttribute("error", ex.getReason() != null ? ex.getReason() : ex.getMessage());
      return "events/new";
    } catch (Exception ex) {
      model.addAttribute("error", ex.getMessage());
      return "events/new";
    }
  }

  @GetMapping("/events/{id}")
  public String eventDetails(
      @PathVariable Long id,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(required = false) String q,
      Model model) {
    populateEventDetails(id, page, q, model);
    if (!model.containsAttribute("registrationForm")) {
      model.addAttribute("registrationForm", new EventRegistrationRequest("", ""));
    }
    return "events/details";
  }

  @PostMapping("/events/{id}/registrations")
  public String register(@PathVariable Long id,
      @Valid @ModelAttribute("registrationForm") EventRegistrationRequest form,
      BindingResult bindingResult,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(required = false) String q,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      populateEventDetails(id, page, q, model);
      return "events/details";
    }
    try {
      registrationService.register(id, form);
      redirectAttributes.addFlashAttribute("success", "Registration successful");
    } catch (ResponseStatusException ex) {
      redirectAttributes.addFlashAttribute("error",
          ex.getReason() != null ? ex.getReason() : ex.getMessage());
    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    String redirect = "redirect:/events/" + id;
    if (q != null && !q.isBlank()) {
      redirect += "?q=" + q;
    }
    return redirect;
  }

  private void populateEventDetails(Long id, int page, String q, Model model) {
    EventResponse event = eventService.getById(id);
    long registered = registrationService.countByEventId(id);
    int seatsLeft = Math.max(0, event.maxSeats() - (int) registered);

    Page<EventRegistrationResponse> registrations = registrationService.searchRegistrations(
        id, q, PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id")));

    model.addAttribute("event", event);
    model.addAttribute("seatsLeft", seatsLeft);
    model.addAttribute("registeredCount", registered);
    model.addAttribute("registrations", registrations);
    model.addAttribute("q", q == null ? "" : q);
  }
}
