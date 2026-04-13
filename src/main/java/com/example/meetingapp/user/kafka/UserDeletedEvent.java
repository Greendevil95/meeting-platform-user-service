package com.example.meetingapp.user.kafka;

import com.example.meetingapp.outbox.UserEventType;
import com.example.meetingapp.user.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserDeletedEvent(
        UUID userId,
        UserStatus status,
        Instant timestamp,
        UUID eventId
) implements UserEvent {
    public static UserDeletedEvent of(UUID userId) {
        return of(userId, UUID.randomUUID());
    }

    public static UserDeletedEvent of(UUID userId, UUID eventId) {
        return new UserDeletedEvent(userId, UserStatus.DELETED, Instant.now(), eventId);
    }

    @Override
    public UserEventType getEventType() {
        return UserEventType.USER_DELETED;
    }
}
