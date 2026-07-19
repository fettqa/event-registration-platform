package com.fettqa.events.event.dto;

import jakarta.validation.constraints.Min;

public record UpdateEventRequest(@Min(1) Integer maxSeats,
                                 String name) {

}
