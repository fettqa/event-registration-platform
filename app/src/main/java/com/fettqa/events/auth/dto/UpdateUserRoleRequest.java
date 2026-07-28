package com.fettqa.events.auth.dto;

import com.fettqa.events.auth.Role;
import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(
    @NotNull Role role
) {}
