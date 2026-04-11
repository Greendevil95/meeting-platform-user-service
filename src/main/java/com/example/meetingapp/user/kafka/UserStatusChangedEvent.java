package com.example.meetingapp.user.kafka;

import com.example.meetingapp.user.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserStatusChangedEvent(
        Long userId,
        UserStatus previousStatus,
        UserStatus status,
        Instant timestamp,
        UUID eventId
) {
    public static UserStatusChangedEvent of(Long userId, UserStatus previousStatus, UserStatus newStatus) {
        return of(userId, previousStatus, newStatus, UUID.randomUUID());
    }

    public static UserStatusChangedEvent of(Long userId, UserStatus previousStatus, UserStatus newStatus, UUID eventId) {
        return new UserStatusChangedEvent(userId, previousStatus, newStatus, Instant.now(), eventId);
    }
}
