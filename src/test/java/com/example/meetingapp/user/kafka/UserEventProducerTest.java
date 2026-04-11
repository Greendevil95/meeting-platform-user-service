package com.example.meetingapp.user.kafka;

import com.example.meetingapp.config.properties.KafkaTopicsProperties;
import com.example.meetingapp.user.entity.Role;
import com.example.meetingapp.user.entity.User;
import com.example.meetingapp.user.entity.UserStatus;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserEventProducerTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaTopicsProperties topics;
    private UserEventProducer producer;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        topics = new KafkaTopicsProperties();
        topics.setUserCreated("topic.user.created");
        topics.setUserUpdated("topic.user.updated");
        topics.setUserDeleted("topic.user.deleted");
        topics.setUserStatusChanged("topic.user.status.changed");
        producer = new UserEventProducer(kafkaTemplate, topics);
        objectMapper = JsonMapper.builder().findAndAddModules().build();
    }

    @Test
    void publishUserCreatedSendsEventIdInPayloadAndHeader() throws Exception {
        User user = createUser(101L);

        producer.publishUserCreated(user);

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertRecordContainsUuidEventId(captor.getValue(), "topic.user.created", "101");
    }

    @Test
    void publishUserUpdatedSendsEventIdInPayloadAndHeader() throws Exception {
        User user = createUser(102L);

        producer.publishUserUpdated(user);

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertRecordContainsUuidEventId(captor.getValue(), "topic.user.updated", "102");
    }

    @Test
    void publishUserDeletedSendsEventIdInPayloadAndHeader() throws Exception {
        producer.publishUserDeleted(103L);

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertRecordContainsUuidEventId(captor.getValue(), "topic.user.deleted", "103");
    }

    @Test
    void publishStatusChangedSendsEventIdInPayloadAndHeader() throws Exception {
        producer.publishStatusChanged(104L, UserStatus.ACTIVE, UserStatus.BANNED);

        ArgumentCaptor<ProducerRecord<String, Object>> captor = ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());
        assertRecordContainsUuidEventId(captor.getValue(), "topic.user.status.changed", "104");
    }

    private User createUser(Long id) {
        User user = new User();
        user.setId(id);
        user.setUsername("user-" + id);
        user.setEmail("user-" + id + "@test.local");
        user.setStatus(UserStatus.ACTIVE);
        user.setRole(Role.USER);
        return user;
    }

    private void assertRecordContainsUuidEventId(
            ProducerRecord<String, Object> record,
            String expectedTopic,
            String expectedKey
    ) throws Exception {
        assertEquals(expectedTopic, record.topic());
        assertEquals(expectedKey, record.key());

        String json = objectMapper.writeValueAsString(record.value());
        JsonNode payload = objectMapper.readTree(json);
        assertTrue(payload.hasNonNull("eventId"), "payload must contain non-null eventId");

        String eventIdText = payload.get("eventId").asText();
        UUID parsed = UUID.fromString(eventIdText);
        assertNotNull(parsed);

        byte[] headerValue = record.headers().lastHeader("event-id").value();
        String headerEventId = new String(headerValue, StandardCharsets.UTF_8);
        assertEquals(eventIdText, headerEventId, "payload eventId must match event-id header");
    }
}
