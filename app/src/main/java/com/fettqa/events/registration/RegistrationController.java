package com.fettqa.events.registration;

import com.fettqa.events.registration.dto.EventRegistrationResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

  @GetMapping
  public List<EventRegistrationResponse> getRegistrations(@PathVariable Long eventId) {
    return registrationService.getRegistrations(eventId);
  }
}
