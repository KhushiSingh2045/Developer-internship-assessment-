package com.example.hotelbooking.dto;

import com.example.hotelbooking.entity.Hotel;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public record HotelResponse(Long id, String name, String location, String description,
                            List<String> amenities, BigDecimal pricePerNight, int totalRooms) {

    public static HotelResponse from(Hotel h) {
        List<String> amenities = (h.getAmenities() == null || h.getAmenities().isBlank())
                ? List.of()
                : Arrays.stream(h.getAmenities().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        return new HotelResponse(h.getId(), h.getName(), h.getLocation(), h.getDescription(),
                amenities, h.getPricePerNight(), h.getTotalRooms());
    }
}
