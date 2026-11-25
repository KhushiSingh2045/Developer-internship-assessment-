package com.example.hotelbooking.repository;

import com.example.hotelbooking.entity.Booking;
import com.example.hotelbooking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByReference(String reference);

    Optional<Booking> findByReferenceAndGuestEmailIgnoreCase(String reference, String guestEmail);

    /**
     * Bookings whose stay overlaps [checkIn, checkOut). Two stays overlap when
     * existing.checkIn < new.checkOut AND existing.checkOut > new.checkIn.
     * A guest leaving on the day another arrives does NOT overlap.
     */
    @Query("""
            SELECT b FROM Booking b
            WHERE b.hotel.id = :hotelId
              AND b.status = :status
              AND b.checkIn < :checkOut
              AND b.checkOut > :checkIn
            """)
    List<Booking> findOverlapping(@Param("hotelId") Long hotelId,
                                  @Param("status") BookingStatus status,
                                  @Param("checkIn") LocalDate checkIn,
                                  @Param("checkOut") LocalDate checkOut);
}
