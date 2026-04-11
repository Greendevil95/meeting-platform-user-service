package com.example.meetingapp.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateUserRequest(
        @Size(min = 1, max = 50)
        String username,

        @Email
        @Size(max = 100)
        String email,

        @Size(max = 50)
        String firstName,

        @Size(max = 50)
        String lastName,

        @Size(max = 255)
        String avatarUrl,

        LocalDate birthDate
) {
}
