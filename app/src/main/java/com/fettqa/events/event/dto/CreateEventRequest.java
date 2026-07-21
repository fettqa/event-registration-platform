package com.fettqa.events.event.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateEventRequest(
    @NotBlank(message = "{event.name.notBlank}")
    String name,
    @Min(value = 1, message = "{event.maxSeats.min}")
    Integer maxSeats
) {

}