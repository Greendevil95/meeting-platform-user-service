package com.example.meetingapp.user.exception;

import org.springframework.http.HttpStatus;

public class UserDeletedException extends ApiServiceException {

    private static final String CODE = "error.user.deleted";

    public UserDeletedException(Long id) {
        super(CODE, "User is deleted with id: " + id, HttpStatus.GONE);
    }
}
