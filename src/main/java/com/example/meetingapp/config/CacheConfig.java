package com.example.meetingapp.config;


import com.example.meetingapp.config.properties.RedisProperties;
import com.example.meetingapp.user.dto.UserResponse;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

@Configuration
public class CacheConfig {

    public static final String USER_CACHE_NAME = "users";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                                     RedisProperties redisProperties,
                                     JsonMapper jsonMapper) {
        RedisCacheConfiguration config = userCacheConfiguration(jsonMapper, redisProperties.getUserTtl());

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }

    static RedisCacheConfiguration userCacheConfiguration(JsonMapper jsonMapper, Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(jsonMapper, UserResponse.class)
                ));
    }
}
