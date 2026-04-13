package com.example.meetingapp.user.kafka;

import com.example.meetingapp.outbox.UserEventType;
import com.example.meetingapp.user.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserUpdatedEvent(
        UUID userId,
        String username,
        String email,
        UserStatus status,
        String role,
        Instant timestamp,
        UUID eventId
) implements UserEvent {
    public static UserUpdatedEvent of(UUID userId, String username, String email, UserStatus status, String role) {
        return of(userId, username, email, status, role, UUID.randomUUID());
    }

    public static UserUpdatedEvent of(UUID userId, String username, String email, UserStatus status, String role, UUID eventId) {
        return new UserUpdatedEvent(userId, username, email, status, role, Instant.now(), eventId);
    }

    @Override
    public UserEventType getEventType() {
        return UserEventType.USER_STATUS_CHANGED;
    }
}
