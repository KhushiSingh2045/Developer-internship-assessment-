package com.example.hotelbooking.service;

import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.BookingStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AvailabilityCalculatorTest {

    private static final LocalDate D1 = LocalDate.of(2030, 5, 1);

    /** Day numbers start at 1: booking(1, 4) means check-in on day 1 and check-out on day 4. */
    private Booking booking(int fromDay, int toDay, BookingStatus status) {
        Booking b = new Booking();
        b.setCheckIn(D1.plusDays(fromDay - 1));
        b.setCheckOut(D1.plusDays(toDay - 1));
        b.setStatus(status);
        return b;
    }

    @Test
    void noBookingsMeansNoOccupancy() {
        assertEquals(0, AvailabilityCalculator.peakOccupancy(List.of(), D1, D1.plusDays(3)));
    }

    @Test
    void overlappingBookingsStackOnTheSameNight() {
        List<Booking> list = List.of(booking(1, 4, BookingStatus.CONFIRMED), booking(2, 5, BookingStatus.CONFIRMED));
        assertEquals(2, AvailabilityCalculator.peakOccupancy(list, D1, D1.plusDays(5)));
    }

    @Test
    void bookingsThatNeverShareANightDoNotAddUp() {
        // Day 1-3 and day 5-7 both sit inside a day 1-7 request but never overlap each other.
        List<Booking> list = List.of(booking(1, 3, BookingStatus.CONFIRMED), booking(5, 7, BookingStatus.CONFIRMED));
        assertEquals(1, AvailabilityCalculator.peakOccupancy(list, D1, D1.plusDays(6)));
    }

    @Test
    void checkOutDayIsNotANightSoBackToBackStaysDoNotConflict() {
        // Existing guest leaves on day 3, new guest arrives on day 3.
        List<Booking> list = List.of(booking(1, 3, BookingStatus.CONFIRMED));
        assertEquals(0, AvailabilityCalculator.peakOccupancy(list, D1.plusDays(2), D1.plusDays(5)));
    }

    @Test
    void cancelledBookingsReleaseTheirRoom() {
        List<Booking> list = List.of(booking(1, 4, BookingStatus.CANCELLED));
        assertEquals(0, AvailabilityCalculator.peakOccupancy(list, D1, D1.plusDays(3)));
    }
}
