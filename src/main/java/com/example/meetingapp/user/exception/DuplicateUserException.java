package com.example.meetingapp.user.exception;

import org.springframework.http.HttpStatus;

public class DuplicateUserException extends ApiServiceException {

    private static final String CODE = "error.user.duplicate";

    public DuplicateUserException(String message) {
        super(CODE, message, HttpStatus.CONFLICT);
    }

    public DuplicateUserException(Long id) {
        super(CODE, "User with such id already exists: " + id, HttpStatus.CONFLICT);
    }
}
