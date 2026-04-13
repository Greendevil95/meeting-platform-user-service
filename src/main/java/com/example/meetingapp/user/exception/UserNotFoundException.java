package com.example.meetingapp.user.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserNotFoundException extends ApiServiceException {

    private static final String CODE = "error.user.not_found";

    public UserNotFoundException(UUID id) {
        super(CODE, "User not found with id: " + id, HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String query) {
        super(CODE, "User not found by query: " + query, HttpStatus.NOT_FOUND);
    }
}
