package com.example.meetingapp.outbox;

import com.example.meetingapp.user.entity.UserStatus;
import com.example.meetingapp.user.kafka.UserCreatedEvent;
import com.example.meetingapp.user.kafka.UserDeletedEvent;
import com.example.meetingapp.user.kafka.UserEvent;
import com.example.meetingapp.user.kafka.UserStatusChangedEvent;
import com.example.meetingapp.user.kafka.UserUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxServiceTest {

    @Mock
    private OutboxEventRepository outboxEventRepository;

    private OutboxService outboxService;
    private JsonMapper jsonMapper;

    @BeforeEach
    void setUp() {
        jsonMapper = JsonMapper.builder().findAndAddModules().build();
        outboxService = new OutboxService(outboxEventRepository, jsonMapper);
    }

    @Test
    void enqueueUserCreated_savesPendingRowWithPayloadRoundTrip() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UserCreatedEvent event = UserCreatedEvent.of(
                userId, "alice", "a@test.local", UserStatus.ACTIVE, "USER", 1L, eventId);

        outboxService.enqueueEvent("USER", userId.toString(), event);

        OutboxEventEntity saved = captureSavedEntity();
        assertOutboxEnvelope(saved, "USER", userId.toString(), UserEventType.USER_CREATED, eventId);
        assertPayloadRoundTrip(saved, UserCreatedEvent.class, eventId);
    }

    @Test
    void enqueueUserUpdated_savesPendingRowWithPayloadRoundTrip() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UserUpdatedEvent event = UserUpdatedEvent.of(
                userId, "bob", "b@test.local", UserStatus.INACTIVE, "ADMIN", 2L, eventId
        );

        outboxService.enqueueEvent("USER", userId.toString(), event);

        OutboxEventEntity saved = captureSavedEntity();
        assertOutboxEnvelope(saved, "USER", userId.toString(), UserEventType.USER_UPDATED, eventId);
        assertPayloadRoundTrip(saved, UserUpdatedEvent.class, eventId);
    }

    @Test
    void enqueueUserDeleted_savesPendingRowWithPayloadRoundTrip() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UserDeletedEvent event = UserDeletedEvent.of(userId, 3L, eventId);

        outboxService.enqueueEvent("USER", userId.toString(), event);

        OutboxEventEntity saved = captureSavedEntity();
        assertOutboxEnvelope(saved, "USER", userId.toString(), UserEventType.USER_DELETED, eventId);
        assertPayloadRoundTrip(saved, UserDeletedEvent.class, eventId);
    }

    @Test
    void enqueueUserStatusChanged_savesPendingRowWithPayloadRoundTrip() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        UserStatusChangedEvent event = UserStatusChangedEvent.of(
                userId, UserStatus.ACTIVE, UserStatus.BANNED, 4L, eventId
        );

        outboxService.enqueueEvent("USER", userId.toString(), event);

        OutboxEventEntity saved = captureSavedEntity();
        assertOutboxEnvelope(saved, "USER", userId.toString(), UserEventType.USER_STATUS_CHANGED, eventId);
        assertPayloadRoundTrip(saved, UserStatusChangedEvent.class, eventId);
    }

    private OutboxEventEntity captureSavedEntity() {
        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(outboxEventRepository).save(captor.capture());
        return captor.getValue();
    }

    private void assertOutboxEnvelope(
            OutboxEventEntity saved,
            String aggregateType,
            String aggregateId,
            UserEventType eventType,
            UUID eventId
    ) {
        assertEquals(aggregateType, saved.getAggregateType());
        assertEquals(aggregateId, saved.getAggregateId());
        assertEquals(eventType, saved.getEventType());
        assertEquals(OutboxStatus.PENDING, saved.getStatus());
        assertEquals(0, saved.getRetryCount());
        assertEquals(eventId, saved.getEventId());
    }

    private <T extends UserEvent> void assertPayloadRoundTrip(OutboxEventEntity saved, Class<T> type, UUID eventId) {
        T restored = jsonMapper.convertValue(saved.getEventJson(), type);
        assertEquals(eventId, restored.eventId());
        assertEquals(saved.getEventId(), restored.eventId());
    }
}
