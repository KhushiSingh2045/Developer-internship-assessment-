package com.example.hotelbooking.service;

import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.BookingStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * Pure logic (no database) so it is easy to unit test.
 * A hotel is only as free as its BUSIEST night: we count the confirmed bookings covering each
 * night of the requested stay and take the maximum.
 */
public final class AvailabilityCalculator {

    private AvailabilityCalculator() {
    }

    public static int peakOccupancy(List<Booking> bookings, LocalDate checkIn, LocalDate checkOut) {
        int peak = 0;
        for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
            int occupied = 0;
            for (Booking b : bookings) {
                boolean coversNight = !night.isBefore(b.getCheckIn()) && night.isBefore(b.getCheckOut());
                if (b.getStatus() == BookingStatus.CONFIRMED && coversNight) {
                    occupied++;
                }
            }
            peak = Math.max(peak, occupied);
        }
        return peak;
    }
}
