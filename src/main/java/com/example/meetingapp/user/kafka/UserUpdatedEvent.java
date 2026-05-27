package com.example.meetingapp.user.kafka;

import com.example.meetingapp.outbox.UserEventType;
import com.example.meetingapp.user.entity.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserUpdatedEvent(
        UUID eventId,
        UUID userId,
        String username,
        String email,
        UserStatus status,
        String role,
        long version,
        OffsetDateTime timestamp
) implements UserEvent {
    public static UserUpdatedEvent of(UUID userId, String username, String email, UserStatus status, String role, long version) {
        return of(userId, username, email, status, role, version, UUID.randomUUID());
    }

    public static UserUpdatedEvent of(UUID userId, String username, String email, UserStatus status, String role, long version, UUID eventId) {
        return new UserUpdatedEvent(eventId, userId, username, email, status, role, version, OffsetDateTime.now());
    }

    @Override
    public UserEventType getEventType() {
        return UserEventType.USER_UPDATED;
    }
}
