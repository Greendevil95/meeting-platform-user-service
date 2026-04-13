package com.example.meetingapp.outbox;

import com.example.meetingapp.user.kafka.*;

public enum UserEventType {
    USER_CREATED(UserCreatedEvent.class),
    USER_UPDATED(UserUpdatedEvent.class),
    USER_DELETED(UserDeletedEvent.class),
    USER_STATUS_CHANGED( UserStatusChangedEvent.class);

    private final Class<? extends UserEvent> eventClass;

    UserEventType(Class<? extends UserEvent> eventClass) {
        this.eventClass = eventClass;
    }

    public Class<? extends UserEvent> eventClass() {
        return eventClass;
    }
}