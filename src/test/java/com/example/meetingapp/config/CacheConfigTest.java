package com.example.meetingapp.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.meetingapp.user.dto.UserInfoResponse;
import com.example.meetingapp.user.dto.UserResponse;
import com.example.meetingapp.user.entity.Role;
import com.example.meetingapp.user.entity.UserStatus;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class CacheConfigTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();

    @Test
    void userCacheDeserializesValuesAsUserResponse() {
        var cacheConfiguration = CacheConfig.userCacheConfiguration(jsonMapper, Duration.ofMinutes(30));
        UserResponse response = new UserResponse(
                UUID.randomUUID(),
                "denis",
                "denis@example.test",
                UserStatus.ACTIVE,
                Role.USER,
                new UserInfoResponse("Denis", "Load", "https://example.test/avatar.png", LocalDate.parse("1995-05-01")),
                Instant.parse("2026-05-31T12:00:00Z")
        );

        var bytes = cacheConfiguration.getValueSerializationPair().write(response);
        Object restored = cacheConfiguration.getValueSerializationPair().read(bytes);

        assertThat(restored).isEqualTo(response);
    }
}
