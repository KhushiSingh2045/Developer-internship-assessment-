package com.example.hotelbooking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

/** One error shape for every failure the API can return. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message,
                            String path, Map<String, String> fieldErrors) {
}
