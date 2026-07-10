package com.fettqa.events.registration;

import com.fettqa.events.registration.dto.EventRegistrationRequest;
import com.fettqa.events.registration.dto.EventRegistrationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistrationController {

  private final RegistrationService registrationService;

  public RegistrationController(RegistrationService registrationService) {
    this.registrationService = registrationService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EventRegistrationResponse register(@PathVariable Long eventId,
      @Valid @RequestBody EventRegistrationRequest request) {
    return registrationService.register(eventId, request);
  }

}
