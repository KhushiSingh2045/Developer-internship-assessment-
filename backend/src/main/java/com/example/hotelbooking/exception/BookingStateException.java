package com.example.hotelbooking.exception;

import org.springframework.http.HttpStatus;

/** 409 - the booking is in a state that does not allow this action (e.g. already cancelled). */
public class BookingStateException extends ApiException {
    public BookingStateException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
