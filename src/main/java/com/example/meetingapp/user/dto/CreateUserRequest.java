package com.example.meetingapp.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(max = 50)
        String username,

        @NotBlank(message = "Email is required")
        @Email
        @Size(max = 100)
        String email,

        @NotBlank(message = "firstName is required")
        @Size(max = 50)
        String firstName,

        @NotBlank(message = "lastName is required")
        @Size(max = 50)
        String lastName
) {
}
