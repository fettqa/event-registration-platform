package com.fettqa.events.auth.dto;

import com.fettqa.events.auth.Role;
import com.fettqa.events.auth.User;

public record UserResponse(
    Long id,
    String fullName,
    String email,
    String role
) {
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getRole().name());
  }
}
