package com.example.meetingapp.user.exception;

import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserDeletedException extends ApiServiceException {

    private static final String CODE = "error.user.deleted";

    public UserDeletedException(UUID id) {
        super(CODE, "User is deleted with id: " + id, HttpStatus.GONE);
    }
}
