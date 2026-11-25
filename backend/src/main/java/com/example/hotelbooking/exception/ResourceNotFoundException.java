package com.example.hotelbooking.exception;

import org.springframework.http.HttpStatus;

/** 404 - hotel or booking not found. */
public class ResourceNotFoundException extends ApiException {
    public ResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
