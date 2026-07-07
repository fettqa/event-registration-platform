package com.fettqa.events.event;

import com.fettqa.events.event.dto.CreateEventRequest;
import com.fettqa.events.event.dto.EventResponse;
import com.fettqa.events.event.dto.UpdateEventRequest;
import io.swagger.v3.oas.annotations.Operation;
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

  @PostMapping("/bulk")
  @ResponseStatus(HttpStatus.CREATED)
  public List<EventResponse> create(@Valid @RequestBody List<@Valid CreateEventRequest> request) {
    return eventService.create(request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    eventService.delete(id);
  }

  @GetMapping("/{id}")
  public EventResponse getById(@PathVariable Long id) {
    return eventService.getById(id);
  }

  @GetMapping(params = "name")
  @Operation(summary = "Find events by name", operationId = "getByName")
  public List<EventResponse> getByName(@RequestParam String name) {
    return eventService.getByName(name);
  }

  @PatchMapping("/{id}")
  public EventResponse updateById(@PathVariable Long id, @Valid @RequestBody UpdateEventRequest request) {
    return eventService.updateById(id, request);
  }

  @GetMapping
  @Operation(summary = "Find all events", operationId = "getAll")
  public List<EventResponse> getAll() {
    return eventService.getAll();
  }
}