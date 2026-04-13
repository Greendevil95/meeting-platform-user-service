package com.example.meetingapp.user.kafka;

import com.example.meetingapp.outbox.UserEventType;

import java.util.UUID;

public interface UserEvent {
    UserEventType getEventType();
    UUID eventId();
}
