package com.fettqa.events.registration;

import com.fettqa.events.registration.dto.EventRegistrationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events/{eventId}/registrations")
public class RegistrationController {

  private final RegistrationService registrationService;

  public RegistrationController(RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @SecurityRequirement(name = "bearerAuth")
  public EventRegistrationResponse register(@PathVariable Long eventId) {
    return registrationService.registerCurrentUser(eventId);
  }

  @GetMapping(params = "page")
  @Operation(summary = "Search registrations with pagination", operationId = "searchRegistrations")
  public Page<EventRegistrationResponse> searchRegistrations(
      @PathVariable Long eventId,
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "100") int size) {
    return registrationService.searchRegistrations(
        eventId, q, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id")));
  }

  @GetMapping
  public List<EventRegistrationResponse> getRegistrations(@PathVariable Long eventId) {
    return registrationService.getRegistrations(eventId);
  }

  @DeleteMapping("/{registrationId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @SecurityRequirement(name = "bearerAuth")
  public void delete(
      @PathVariable Long eventId,
      @PathVariable Long registrationId) {
    registrationService.deleteRegistration(eventId, registrationId);
  }
}
