package com.fettqa.events.auth.dto;

import com.fettqa.events.auth.User;

public record AuthResponse(
    String accessToken,
    String tokenType,
    String fullName,
    String email,
    String role
) {

  public static AuthResponse bearer(String token, User user) {
    return new AuthResponse(
        token,
        "Bearer",
        user.getFullName(),
        user.getEmail(),
        user.getRole().name());
  }
}
