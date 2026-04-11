package com.example.meetingapp.user.dto;

import com.example.meetingapp.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull(message = "Status is required")
        UserStatus status
) {
}
