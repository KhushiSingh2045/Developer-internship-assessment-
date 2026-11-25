package com.example.hotelbooking.controller;

import com.example.hotelbooking.dto.BookingRequest;
import com.example.hotelbooking.dto.BookingResponse;
import com.example.hotelbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody BookingRequest request) {
        return bookingService.create(request);
    }

    @GetMapping("/{reference}")
    public BookingResponse get(@PathVariable String reference, @RequestParam String email) {
        return bookingService.get(reference, email);
    }

    @PostMapping("/{reference}/cancel")
    public BookingResponse cancel(@PathVariable String reference, @RequestParam String email) {
        return bookingService.cancel(reference, email);
    }

    /** Surprise feature: download the stay as an .ics calendar file. */
    @GetMapping("/{reference}/calendar")
    public ResponseEntity<byte[]> calendar(@PathVariable String reference, @RequestParam String email) {
        String ics = bookingService.calendarFile(reference, email);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/calendar; charset=UTF-8")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"stay-" + reference + ".ics\"")
                .body(ics.getBytes(StandardCharsets.UTF_8));
    }
}
