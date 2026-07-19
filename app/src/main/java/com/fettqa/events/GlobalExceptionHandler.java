package com.fettqa.events;

import com.fettqa.events.event.EventNotFoundException;
import com.fettqa.events.registration.RegistrationConflictException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

  @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fieldErrors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
        fieldErrors.put(error.getField(), error.getDefaultMessage())
    );
    return Map.of(
        "error", "Validation failed",
        "fields", fieldErrors
    );
  }
}
