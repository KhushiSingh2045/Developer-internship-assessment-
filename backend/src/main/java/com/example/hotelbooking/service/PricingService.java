package com.example.hotelbooking.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** All money maths lives here, on the backend. The client never sends a price. */
@Service
public class PricingService {

    /** Nights stayed = days between check-in and check-out (the check-out day is not a night). */
    public int nights(LocalDate checkIn, LocalDate checkOut) {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public BigDecimal total(BigDecimal pricePerNight, int nights) {
        return pricePerNight.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);
    }
}
