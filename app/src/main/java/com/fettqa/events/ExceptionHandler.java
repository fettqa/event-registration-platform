package com.fettqa.events;

import com.fettqa.events.event.EventNotFoundException;
import com.fettqa.events.registration.RegistrationConflictException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandler {
  @org.springframework.web.bind.annotation.ExceptionHandler(EventNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public Map<String, String> handleNotFound(EventNotFoundException ex) {
    return Map.of("error", ex.getMessage());
  }

  @org.springframework.web.bind.annotation.ExceptionHandler(RegistrationConflictException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  public Map<String, String> handleConflict(RegistrationConflictException ex) {
    return Map.of("error", ex.getMessage());
  }
}
