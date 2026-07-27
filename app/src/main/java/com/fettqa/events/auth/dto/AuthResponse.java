package com.fettqa.events.auth.dto;

import com.fettqa.events.auth.User;

public record AuthResponse(
    String accessToken,
    String tokenType,
    String email,
    String role
) {

  public static AuthResponse bearer(String token, User user) {
    return new AuthResponse(token, "Bearer", user.getEmail(), user.getRole().name());
  }
}