package com.example.meetingapp.user.dto;

import java.time.LocalDate;

public record UserInfoResponse(
        String firstName,
        String lastName,
        String avatarUrl,
        LocalDate birthDate
) {
}
