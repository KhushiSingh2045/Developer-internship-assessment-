package com.example.hotelbooking.exception;

import org.springframework.http.HttpStatus;

/** Base class for expected, business-level failures. Carries the HTTP status to return. */
public abstract class ApiException extends RuntimeException {

    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
