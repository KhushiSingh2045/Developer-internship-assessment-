package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.AvailabilityResponse;
import com.example.hotelbooking.dto.BookingRequest;
import com.example.hotelbooking.dto.BookingResponse;
import com.example.hotelbooking.dto.HotelResponse;
import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.BookingStatus;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.exception.BookingStateException;
import com.example.hotelbooking.exception.InvalidBookingException;
import com.example.hotelbooking.exception.NoAvailabilityException;
import com.example.hotelbooking.exception.ResourceNotFoundException;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {

    static final int MAX_NIGHTS = 30;

    private final HotelRepository hotels;
    private final BookingRepository bookings;
    private final PricingService pricing;
    private final CalendarService calendar;
    private final Clock clock;

    public BookingService(HotelRepository hotels, BookingRepository bookings, PricingService pricing,
                          CalendarService calendar, Clock clock) {
        this.hotels = hotels;
        this.bookings = bookings;
        this.pricing = pricing;
        this.calendar = calendar;
        this.clock = clock;
    }

    /** Read-only: how many rooms are free for this stay and what it would cost. */
    @Transactional(readOnly = true)
    public AvailabilityResponse checkAvailability(Long hotelId, LocalDate checkIn, LocalDate checkOut) {
        validateDates(checkIn, checkOut);
        Hotel hotel = hotels.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found."));
        return buildAvailability(hotel, checkIn, checkOut);
    }

    /**
     * Creates a booking. The hotel row is locked first, so if two guests go for the last room the
     * second one waits, then re-checks availability and gets a clean "no rooms left" error.
     */
    @Transactional
    public BookingResponse create(BookingRequest request) {
        validateDates(request.checkIn(), request.checkOut());

        Hotel hotel = hotels.findByIdForUpdate(request.hotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found."));

        AvailabilityResponse availability = buildAvailability(hotel, request.checkIn(), request.checkOut());
        if (availability.availableRooms() <= 0) {
            throw new NoAvailabilityException(
                    "Sorry, " + hotel.getName() + " has no rooms left for these dates. Try different dates.");
        }

        Booking booking = new Booking();
        booking.setReference(generateReference());
        booking.setHotel(hotel);
        booking.setGuestName(request.guestName().trim());
        booking.setGuestEmail(request.guestEmail().trim().toLowerCase());
        booking.setCheckIn(request.checkIn());
        booking.setCheckOut(request.checkOut());
        booking.setNights(availability.nights());
        booking.setTotalAmount(availability.totalAmount());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(LocalDateTime.now(clock));

        return BookingResponse.from(bookings.save(booking));
    }

    @Transactional(readOnly = true)
    public BookingResponse get(String reference, String email) {
        return BookingResponse.from(find(reference, email));
    }

    /** Cancelling flips the status; cancelled bookings are ignored by the availability count, which frees the room. */
    @Transactional
    public BookingResponse cancel(String reference, String email) {
        Booking booking = find(reference, email);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingStateException("Booking " + booking.getReference() + " is already cancelled.");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return BookingResponse.from(bookings.save(booking));
    }

    @Transactional(readOnly = true)
    public String calendarFile(String reference, String email) {
        Booking booking = find(reference, email);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new BookingStateException("This booking is cancelled, so there is nothing to add to a calendar.");
        }
        return calendar.toIcs(booking);
    }

    // ---- helpers ----

    private AvailabilityResponse buildAvailability(Hotel hotel, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> overlapping = bookings.findOverlapping(hotel.getId(), BookingStatus.CONFIRMED, checkIn, checkOut);
        int occupied = AvailabilityCalculator.peakOccupancy(overlapping, checkIn, checkOut);
        int available = Math.max(0, hotel.getTotalRooms() - occupied);
        int nights = pricing.nights(checkIn, checkOut);
        return new AvailabilityResponse(HotelResponse.from(hotel), checkIn, checkOut, nights, available,
                available > 0, pricing.total(hotel.getPricePerNight(), nights));
    }

    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) {
            throw new InvalidBookingException("Check-in and check-out dates are required.");
        }
        if (checkIn.isBefore(LocalDate.now(clock))) {
            throw new InvalidBookingException("Check-in date cannot be in the past.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidBookingException("Check-out date must be after the check-in date.");
        }
        if (pricing.nights(checkIn, checkOut) > MAX_NIGHTS) {
            throw new InvalidBookingException("A single booking can be at most " + MAX_NIGHTS + " nights.");
        }
    }

    private Booking find(String reference, String email) {
        // Same message for "wrong reference" and "wrong email" so references cannot be guessed.
        return bookings.findByReferenceAndGuestEmailIgnoreCase(reference.trim().toUpperCase(), email.trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "We could not find a booking with that reference and email."));
    }

    private String generateReference() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String ref = "BK-" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            if (!bookings.existsByReference(ref)) {
                return ref;
            }
        }
        throw new IllegalStateException("Could not generate a unique booking reference.");
    }
}
