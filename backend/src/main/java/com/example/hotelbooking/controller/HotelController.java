package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.AvailabilityResponse;
import com.example.hotelbooking.dto.HotelResponse;
import com.example.hotelbooking.service.BookingService;
import com.example.hotelbooking.service.HotelService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;
    private final BookingService bookingService;

    public HotelController(HotelService hotelService, BookingService bookingService) {
        this.hotelService = hotelService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<HotelResponse> search(@RequestParam(required = false) String location) {
        return hotelService.search(location);
    }

    @GetMapping("/{id}")
    public HotelResponse get(@PathVariable Long id) {
        return hotelService.get(id);
    }

    @GetMapping("/{id}/availability")
    public AvailabilityResponse availability(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return bookingService.checkAvailability(id, checkIn, checkOut);
    }
}
