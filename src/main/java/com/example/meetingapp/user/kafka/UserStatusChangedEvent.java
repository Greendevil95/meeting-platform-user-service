package com.example.meetingapp.user.kafka;

import com.example.meetingapp.outbox.UserEventType;
import com.example.meetingapp.user.entity.UserStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserStatusChangedEvent(
        UUID eventId,
        UUID userId,
        UserStatus previousStatus,
        UserStatus status,
        long version,
        OffsetDateTime timestamp
) implements UserEvent {
    public static UserStatusChangedEvent of(UUID userId, UserStatus previousStatus, UserStatus newStatus, long version) {
        return of(userId, previousStatus, newStatus, version, UUID.randomUUID());
    }

    public static UserStatusChangedEvent of(UUID userId, UserStatus previousStatus, UserStatus newStatus, long version, UUID eventId) {
        return new UserStatusChangedEvent(eventId, userId, previousStatus, newStatus, version, OffsetDateTime.now());
    }

    @Override
    public UserEventType getEventType() {
        return UserEventType.USER_STATUS_CHANGED;
    }
}
