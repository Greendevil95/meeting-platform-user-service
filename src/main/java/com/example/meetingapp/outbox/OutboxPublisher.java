package com.example.meetingapp.outbox;

import com.example.meetingapp.config.properties.KafkaTopicsProperties;
import com.example.meetingapp.user.kafka.UserEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.OffsetDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTopicsProperties topicsProperties;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final JsonMapper jsonMapper;

    @Transactional
    @Scheduled(fixedDelayString = "PT3S")
    public void publishPending() {
        List<OutboxEventEntity> pending = outboxEventRepository.findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        for (OutboxEventEntity event : pending) {
            try {
                UserEvent kafkaEvent = jsonMapper.convertValue(event.getEventJson(), event.getEventType().eventClass());
                kafkaTemplate.send(resolveTopic(event.getEventType()), event.getAggregateId(), kafkaEvent).get();
                event.setStatus(OutboxStatus.PUBLISHED);
                event.setPublishedAt(OffsetDateTime.now());
                outboxEventRepository.save(event);
            } catch (Exception ex) {
                event.setStatus(OutboxStatus.FAILED);
                event.setRetryCount(event.getRetryCount() + 1);
                outboxEventRepository.save(event);
                log.error("Cannot publish outbox event {}", event.getId(), ex);
            }
        }
    }

    private String resolveTopic(UserEventType eventType) {
        return switch (eventType) {
            case USER_CREATED -> topicsProperties.getUserCreated();
            case USER_UPDATED -> topicsProperties.getUserUpdated();
            case USER_DELETED -> topicsProperties.getUserDeleted();
            case USER_STATUS_CHANGED -> topicsProperties.getUserStatusChanged();
        };
    }
}
