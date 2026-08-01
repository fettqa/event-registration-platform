package com.fettqa.events.android.model

data class LoginRequest(
    val email: String,
    val password: String,
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
)

data class AuthResponse(
    val accessToken: String,
    val tokenType: String?,
    val fullName: String?,
    val email: String?,
    val role: String?,
)

data class CreateEventRequest(
    val name: String,
    val maxSeats: Int,
)

data class EventResponse(
    val id: Long,
    val name: String,
    val maxSeats: Int,
    val createdById: Long?,
    val createdByEmail: String?,
    val createdAt: String?,
)

data class EventRegistrationResponse(
    val id: Long,
    val eventId: Long,
    val email: String?,
    val fullName: String?,
    val createdAt: String?,
)

data class UserResponse(
    val id: Long,
    val fullName: String?,
    val email: String?,
    val role: String?,
)

data class UpdateUserRoleRequest(
    val role: String,
)

data class ApiError(
    val error: String?,
)
