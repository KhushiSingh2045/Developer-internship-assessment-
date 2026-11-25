package com.example.hotelbooking.exception;

import org.springframework.http.HttpStatus;

/** 400 - the request is well-formed but breaks a booking rule (e.g. dates). */
public class InvalidBookingException extends ApiException {
    public InvalidBookingException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
