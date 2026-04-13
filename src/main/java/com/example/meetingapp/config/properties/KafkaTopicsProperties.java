package com.example.meetingapp.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "user.kafka.topics.user-service")
@Getter
@Setter
public class KafkaTopicsProperties {

    private String userCreated;
    private String userUpdated;
    private String userDeleted;
    private String userStatusChanged;
}
