package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.BookingRequest;
import com.example.hotelbooking.dto.BookingResponse;
import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.BookingStatus;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.exception.BookingStateException;
import com.example.hotelbooking.exception.InvalidBookingException;
import com.example.hotelbooking.exception.NoAvailabilityException;
import com.example.hotelbooking.exception.ResourceNotFoundException;
import com.example.hotelbooking.repository.BookingRepository;
import com.example.hotelbooking.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2030, 1, 10);

    @Mock
    private HotelRepository hotels;
    @Mock
    private BookingRepository bookings;

    private BookingService service;

    @BeforeEach
    void setUp() {
        Clock fixed = Clock.fixed(Instant.parse("2030-01-10T10:00:00Z"), ZoneOffset.UTC);
        service = new BookingService(hotels, bookings, new PricingService(), new CalendarService(), fixed);
    }

    private Hotel hotel(int rooms) {
        Hotel h = new Hotel();
        h.setId(1L);
        h.setName("Test Hotel");
        h.setLocation("Goa");
        h.setPricePerNight(new BigDecimal("100.00"));
        h.setTotalRooms(rooms);
        return h;
    }

    private Booking booking(Hotel h, LocalDate in, LocalDate out, BookingStatus status) {
        Booking b = new Booking();
        b.setReference("BK-TEST0001");
        b.setHotel(h);
        b.setGuestName("Existing Guest");
        b.setGuestEmail("guest@example.com");
        b.setCheckIn(in);
        b.setCheckOut(out);
        b.setNights((int) ChronoUnit.DAYS.between(in, out));
        b.setTotalAmount(new BigDecimal("300.00"));
        b.setStatus(status);
        return b;
    }

    private BookingRequest request(LocalDate in, LocalDate out) {
        return new BookingRequest(1L, "Asha", "Asha@Example.com", in, out);
    }

    private void stubSave() {
        when(bookings.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void createsBookingWithBackendCalculatedTotal() {
        when(hotels.findByIdForUpdate(1L)).thenReturn(Optional.of(hotel(2)));
        when(bookings.findOverlapping(eq(1L), eq(BookingStatus.CONFIRMED), any(), any())).thenReturn(List.of());
        stubSave();

        BookingResponse response = service.create(request(TODAY.plusDays(5), TODAY.plusDays(8)));

        assertEquals(3, response.nights());
        assertEquals(new BigDecimal("300.00"), response.totalAmount());
        assertEquals(BookingStatus.CONFIRMED, response.status());
        assertTrue(response.reference().startsWith("BK-"));
        assertEquals("asha@example.com", response.guestEmail());
    }

    @Test
    void rejectsBookingWhenHotelIsFull() {
        Hotel h = hotel(1);
        LocalDate in = TODAY.plusDays(5);
        LocalDate out = TODAY.plusDays(7);
        when(hotels.findByIdForUpdate(1L)).thenReturn(Optional.of(h));
        when(bookings.findOverlapping(eq(1L), eq(BookingStatus.CONFIRMED), any(), any()))
                .thenReturn(List.of(booking(h, in, out, BookingStatus.CONFIRMED)));

        assertThrows(NoAvailabilityException.class, () -> service.create(request(in, out)));
        verify(bookings, never()).save(any());
    }

    @Test
    void allowsBookingThatStartsOnTheDayAnotherGuestLeaves() {
        Hotel h = hotel(1);
        LocalDate leaveDay = TODAY.plusDays(5);
        when(hotels.findByIdForUpdate(1L)).thenReturn(Optional.of(h));
        // Even if the repository handed this booking back, its check-out day is not a night.
        when(bookings.findOverlapping(eq(1L), eq(BookingStatus.CONFIRMED), any(), any()))
                .thenReturn(List.of(booking(h, TODAY.plusDays(3), leaveDay, BookingStatus.CONFIRMED)));
        stubSave();

        BookingResponse response = service.create(request(leaveDay, leaveDay.plusDays(2)));

        assertEquals(BookingStatus.CONFIRMED, response.status());
    }

    @Test
    void rejectsCheckOutOnOrBeforeCheckIn() {
        LocalDate in = TODAY.plusDays(5);
        assertThrows(InvalidBookingException.class, () -> service.create(request(in, in)));
        assertThrows(InvalidBookingException.class, () -> service.create(request(in, in.minusDays(1))));
    }

    @Test
    void rejectsCheckInInThePast() {
        InvalidBookingException ex = assertThrows(InvalidBookingException.class,
                () -> service.create(request(TODAY.minusDays(1), TODAY.plusDays(2))));
        assertTrue(ex.getMessage().contains("past"));
    }

    @Test
    void allowsCheckInToday() {
        when(hotels.findByIdForUpdate(1L)).thenReturn(Optional.of(hotel(1)));
        when(bookings.findOverlapping(eq(1L), eq(BookingStatus.CONFIRMED), any(), any())).thenReturn(List.of());
        stubSave();

        assertDoesNotThrow(() -> service.create(request(TODAY, TODAY.plusDays(1))));
    }

    @Test
    void rejectsStaysLongerThanTheMaximum() {
        assertThrows(InvalidBookingException.class,
                () -> service.create(request(TODAY.plusDays(1), TODAY.plusDays(2 + BookingService.MAX_NIGHTS))));
    }

    @Test
    void unknownHotelIsNotFound() {
        when(hotels.findByIdForUpdate(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.create(request(TODAY.plusDays(2), TODAY.plusDays(3))));
    }

    @Test
    void cancellingMarksBookingCancelledSoItNoLongerCountsAgainstAvailability() {
        Booking existing = booking(hotel(1), TODAY.plusDays(2), TODAY.plusDays(4), BookingStatus.CONFIRMED);
        when(bookings.findByReferenceAndGuestEmailIgnoreCase("BK-TEST0001", "guest@example.com"))
                .thenReturn(Optional.of(existing));
        stubSave();

        BookingResponse response = service.cancel("bk-test0001", "guest@example.com");

        assertEquals(BookingStatus.CANCELLED, response.status());
        assertEquals(0, AvailabilityCalculator.peakOccupancy(List.of(existing), TODAY.plusDays(2), TODAY.plusDays(4)));
    }

    @Test
    void cancellingTwiceFails() {
        Booking existing = booking(hotel(1), TODAY.plusDays(2), TODAY.plusDays(4), BookingStatus.CANCELLED);
        when(bookings.findByReferenceAndGuestEmailIgnoreCase("BK-TEST0001", "guest@example.com"))
                .thenReturn(Optional.of(existing));

        assertThrows(BookingStateException.class, () -> service.cancel("BK-TEST0001", "guest@example.com"));
    }

    @Test
    void wrongEmailOrReferenceIsNotFound() {
        when(bookings.findByReferenceAndGuestEmailIgnoreCase("BK-NOPE", "x@example.com"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.get("BK-NOPE", "x@example.com"));
    }
}
