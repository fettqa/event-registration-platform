package com.fettqa.events.event.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateEventRequest(
    @NotBlank(message = "name must not be blank")
    String name,
    @Min(value = 1, message = "maxSeats must be at least 1")
    Integer maxSeats
) {
}