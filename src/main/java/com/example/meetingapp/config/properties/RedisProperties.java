package com.example.meetingapp.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "user.redis")
@Getter
@Setter
public class RedisProperties {

    private Duration userTtl;
}
