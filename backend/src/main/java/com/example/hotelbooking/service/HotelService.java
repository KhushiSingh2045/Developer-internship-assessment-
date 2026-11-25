package com.example.hotelbooking.service;

import com.example.hotelbooking.dto.HotelResponse;
import com.example.hotelbooking.entity.Hotel;
import com.example.hotelbooking.exception.ResourceNotFoundException;
import com.example.hotelbooking.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HotelService {

    private final HotelRepository hotels;

    public HotelService(HotelRepository hotels) {
        this.hotels = hotels;
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> search(String location) {
        List<Hotel> found = (location == null || location.isBlank())
                ? hotels.findAllByOrderByNameAsc()
                : hotels.findByLocationContainingIgnoreCaseOrderByNameAsc(location.trim());
        return found.stream().map(HotelResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public HotelResponse get(Long id) {
        return HotelResponse.from(hotels.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found.")));
    }
}
