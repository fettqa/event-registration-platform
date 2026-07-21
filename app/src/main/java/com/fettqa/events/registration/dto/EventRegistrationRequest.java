package com.fettqa.events.registration.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EventRegistrationRequest(
    @NotBlank(message = "{registration.fullName.notBlank}") String fullName,
    @NotBlank(message = "{registration.email.notBlank}") @Email(message = "{registration.email.invalid}") String email) {

}
