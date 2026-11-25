package com.example.hotelbooking.exception;

import org.springframework.http.HttpStatus;

/** 409 - no room left for (part of) the requested stay. */
public class NoAvailabilityException extends ApiException {
    public NoAvailabilityException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
