package com.example.meetingapp.user.entity;

import jakarta.persistence.Version;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserVersionTest {

    @Test
    void versionUsesJpaOptimisticLocking() throws NoSuchFieldException {
        Version version = User.class.getDeclaredField("version").getAnnotation(Version.class);

        assertNotNull(version);
    }
}
