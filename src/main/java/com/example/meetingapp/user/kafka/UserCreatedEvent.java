package com.example.meetingapp.user.kafka;

import com.example.meetingapp.user.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserCreatedEvent(
        Long userId,
        String username,
        String email,
        UserStatus status,
        String role,
        Instant timestamp,
        UUID eventId
) {
    public static UserCreatedEvent of(Long userId, String username, String email, UserStatus status, String role) {
        return of(userId, username, email, status, role, UUID.randomUUID());
    }

    public static UserCreatedEvent of(Long userId, String username, String email, UserStatus status, String role, UUID eventId) {
        return new UserCreatedEvent(userId, username, email, status, role, Instant.now(), eventId);
    }
}
