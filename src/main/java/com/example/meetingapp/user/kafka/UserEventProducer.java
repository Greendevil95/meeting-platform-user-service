package com.example.meetingapp.user.kafka;

import com.example.meetingapp.config.properties.KafkaTopicsProperties;
import com.example.meetingapp.user.entity.User;
import com.example.meetingapp.user.entity.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    public void publishUserCreated(User user) {
        UserCreatedEvent event = UserCreatedEvent.of(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus(),
                user.getRole().name()
        );
        sendWithEventIdHeader(topics.getUserCreated(), String.valueOf(user.getId()), event, event.eventId());
        log.info("Published user.created for userId={}", user.getId());
    }

    public void publishUserUpdated(User user) {
        UserUpdatedEvent event = UserUpdatedEvent.of(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus(),
                user.getRole().name()
        );
        sendWithEventIdHeader(topics.getUserUpdated(), String.valueOf(user.getId()), event, event.eventId());
        log.info("Published user.updated for userId={}", user.getId());
    }

    public void publishUserDeleted(Long userId) {
        UserDeletedEvent event = UserDeletedEvent.of(userId);
        sendWithEventIdHeader(topics.getUserDeleted(), String.valueOf(userId), event, event.eventId());
        log.info("Published user.deleted for userId={}", userId);
    }

    public void publishStatusChanged(Long userId, UserStatus previousStatus, UserStatus newStatus) {
        UserStatusChangedEvent event = UserStatusChangedEvent.of(userId, previousStatus, newStatus);
        sendWithEventIdHeader(topics.getUserStatusChanged(), String.valueOf(userId), event, event.eventId());
        log.info("Published user.status.changed for userId={}, {} -> {}", userId, previousStatus, newStatus);
    }

    private void sendWithEventIdHeader(String topic, String key, Object payload, UUID eventId) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, payload);
        record.headers().add("event-id", eventId.toString().getBytes(StandardCharsets.UTF_8));
        kafkaTemplate.send(record);
    }
}
