package ru.mifi.practice.hotel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mifi.practice.hotel.dto.RoomResponse;
import ru.mifi.practice.hotel.model.Room;

@org.springframework.stereotype.Component
public class RoomMapper {

    public RoomResponse toResponse(Room room) {
        Long hotelId = room.getHotel() != null ? room.getHotel().getId() : null;
        return new RoomResponse(room.getId(), hotelId, room.getNumber(), room.isAvailable(), room.getTimesBooked());
    }
}
