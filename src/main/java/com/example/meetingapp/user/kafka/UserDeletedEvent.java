package com.example.meetingapp.user.kafka;

import com.example.meetingapp.outbox.UserEventType;
import com.example.meetingapp.user.entity.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserDeletedEvent(
        UUID eventId,
        UUID userId,
        UserStatus status,
        long version,
        OffsetDateTime timestamp
) implements UserEvent {
    public static UserDeletedEvent of(UUID userId, long version) {
        return of(userId, version, UUID.randomUUID());
    }

    public static UserDeletedEvent of(UUID userId, long version, UUID eventId) {
        return new UserDeletedEvent(eventId, userId, UserStatus.DELETED, version, OffsetDateTime.now());
    }

    @Override
    public UserEventType getEventType() {
        return UserEventType.USER_DELETED;
    }
}
