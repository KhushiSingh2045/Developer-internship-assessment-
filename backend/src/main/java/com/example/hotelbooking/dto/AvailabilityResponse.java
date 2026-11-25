package com.example.hotelbooking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AvailabilityResponse(HotelResponse hotel, LocalDate checkIn, LocalDate checkOut,
                                   int nights, int availableRooms, boolean available,
                                   BigDecimal totalAmount) {
}
