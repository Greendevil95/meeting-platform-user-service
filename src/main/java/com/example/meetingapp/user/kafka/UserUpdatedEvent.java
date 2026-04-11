package com.example.meetingapp.user.kafka;

import com.example.meetingapp.user.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserUpdatedEvent(
        Long userId,
        String username,
        String email,
        UserStatus status,
        String role,
        Instant timestamp,
        UUID eventId
) {
    public static UserUpdatedEvent of(Long userId, String username, String email, UserStatus status, String role) {
        return of(userId, username, email, status, role, UUID.randomUUID());
    }

    public static UserUpdatedEvent of(Long userId, String username, String email, UserStatus status, String role, UUID eventId) {
        return new UserUpdatedEvent(userId, username, email, status, role, Instant.now(), eventId);
    }
}
