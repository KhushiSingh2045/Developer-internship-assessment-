package com.example.hotelbooking.repository;

import com.example.hotelbooking.entity.Hotel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByLocationContainingIgnoreCaseOrderByNameAsc(String location);

    List<Hotel> findAllByOrderByNameAsc();

    /** Takes a row lock (SELECT ... FOR UPDATE) so two bookings for the same hotel are processed one at a time. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT h FROM Hotel h WHERE h.id = :id")
    Optional<Hotel> findByIdForUpdate(@Param("id") Long id);
}
