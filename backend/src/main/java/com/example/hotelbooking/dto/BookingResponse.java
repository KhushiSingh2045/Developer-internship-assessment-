package com.example.hotelbooking.dto;

import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(String reference, Long hotelId, String hotelName, String hotelLocation,
                              String guestName, String guestEmail, LocalDate checkIn, LocalDate checkOut,
                              int nights, BigDecimal totalAmount, BookingStatus status,
                              LocalDateTime createdAt) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getReference(), b.getHotel().getId(), b.getHotel().getName(),
                b.getHotel().getLocation(), b.getGuestName(), b.getGuestEmail(), b.getCheckIn(),
                b.getCheckOut(), b.getNights(), b.getTotalAmount(), b.getStatus(), b.getCreatedAt());
    }
}
