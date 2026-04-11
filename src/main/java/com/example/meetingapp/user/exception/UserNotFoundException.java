package com.example.meetingapp.user.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiServiceException {

    private static final String CODE = "error.user.not_found";

    public UserNotFoundException(Long id) {
        super(CODE, "User not found with id: " + id, HttpStatus.NOT_FOUND);
    }

    public UserNotFoundException(String query) {
        super(CODE, "User not found by query: " + query, HttpStatus.NOT_FOUND);
    }
}
