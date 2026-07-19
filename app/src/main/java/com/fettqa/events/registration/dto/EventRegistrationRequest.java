package com.fettqa.events.registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EventRegistrationRequest(@NotBlank String fullName,
                                       @NotBlank @Email String email) {

}
