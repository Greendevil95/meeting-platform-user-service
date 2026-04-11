package com.example.meetingapp.user.kafka;

import com.example.meetingapp.user.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserDeletedEvent(
        Long userId,
        UserStatus status,
        Instant timestamp,
        UUID eventId
) {
    public static UserDeletedEvent of(Long userId) {
        return of(userId, UUID.randomUUID());
    }

    public static UserDeletedEvent of(Long userId, UUID eventId) {
        return new UserDeletedEvent(userId, UserStatus.DELETED, Instant.now(), eventId);
    }
}
