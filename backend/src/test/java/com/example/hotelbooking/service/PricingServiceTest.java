package com.example.hotelbooking.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {

    private final PricingService pricing = new PricingService();

    @Test
    void nightsIsDaysBetweenCheckInAndCheckOut() {
        assertEquals(3, pricing.nights(LocalDate.of(2030, 3, 1), LocalDate.of(2030, 3, 4)));
    }

    @Test
    void nightsCountsAcrossMonthEnd() {
        assertEquals(2, pricing.nights(LocalDate.of(2030, 1, 31), LocalDate.of(2030, 2, 2)));
    }

    @Test
    void totalIsPriceTimesNightsWithTwoDecimals() {
        assertEquals(new BigDecimal("13000.00"), pricing.total(new BigDecimal("6500.00"), 2));
        assertEquals(new BigDecimal("1349.97"), pricing.total(new BigDecimal("449.99"), 3));
    }
}
