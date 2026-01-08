package ru.mifi.practice.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mifi.practice.hotel.dto.HotelRequest;
import ru.mifi.practice.hotel.model.Hotel;
import ru.mifi.practice.hotel.repository.HotelRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelCrudService {

    private final HotelRepository hotelRepository;

    @Transactional
    public Hotel create(HotelRequest request) {
        Hotel hotel = Hotel.builder()
                .name(request.name())
                .address(request.address())
                .build();
        return hotelRepository.save(hotel);
    }

    @Transactional(readOnly = true)
    public List<Hotel> findAll() {
        return hotelRepository.findAll();
    }
}
