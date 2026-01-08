package ru.mifi.practice.hotel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.mifi.practice.hotel.dto.RoomRequest;
import ru.mifi.practice.hotel.model.Hotel;
import ru.mifi.practice.hotel.model.Room;
import ru.mifi.practice.hotel.repository.HotelRepository;
import ru.mifi.practice.hotel.repository.RoomRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomCrudService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    @Transactional
    public Room create(RoomRequest request) {
        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));
        Room room = Room.builder()
                .hotel(hotel)
                .number(request.number())
                .available(request.available())
                .timesBooked(0)
                .build();
        return roomRepository.save(room);
    }

    @Transactional(readOnly = true)
    public List<Room> findAllAvailable() {
        return roomRepository.findByAvailableTrue();
    }

    @Transactional(readOnly = true)
    public List<Room> recommended() {
        return roomRepository.findRecommended();
    }

    @Transactional(readOnly = true)
    public List<Room> stats(Boolean available) {
        if (available == null) {
            return roomRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "timesBooked"));
        }
        return roomRepository.findAllByAvailable(available, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "timesBooked"));
    }
}
