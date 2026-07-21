package com.fettqa.events.event.dto;

import jakarta.validation.constraints.Min;

public record UpdateEventRequest(@Min(value = 1, message = "{event.maxSeats.min}") Integer maxSeats,
                                 String name) {

}
