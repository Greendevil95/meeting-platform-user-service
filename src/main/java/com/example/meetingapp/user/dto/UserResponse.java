package com.example.meetingapp.user.dto;

import com.example.meetingapp.user.entity.Role;
import com.example.meetingapp.user.entity.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        UserStatus status,
        Role role,
        UserInfoResponse userInfo,
        Instant createdAt
) {
}
