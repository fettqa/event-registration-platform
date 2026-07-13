package com.fettqa.events.registration.dto;

import jakarta.validation.constraints.Email;

public record EventRegistrationRequest(String fullName, @Email String email) {}
