package com.fettqa.events.event;

import com.fettqa.events.event.dto.CreateEventRequest;
import com.fettqa.events.event.dto.EventResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventController {

  private final EventService eventService;

  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EventResponse create(@Valid @RequestBody CreateEventRequest request) {
    return eventService.create(request);
  }

  @GetMapping("/{id}")
  public EventResponse getById(@PathVariable Long id) {
    return eventService.getById(id);
  }

  @GetMapping
  public List<EventResponse> getAll() {
    return eventService.getAll();
  }
}