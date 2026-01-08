package ru.mifi.practice.hotel.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mifi.practice.hotel.dto.HotelResponse;
import ru.mifi.practice.hotel.dto.RoomShortResponse;
import ru.mifi.practice.hotel.model.Hotel;
import ru.mifi.practice.hotel.model.Room;

import java.util.List;

@org.springframework.stereotype.Component
public class HotelMapper {

    public HotelResponse toResponse(Hotel hotel) {
        return new HotelResponse(
                hotel.getId(),
                hotel.getName(),
                hotel.getAddress(),
                toShortList(hotel.getRooms())
        );
    }

    private RoomShortResponse toShort(Room room) {
        if (room == null) {
            return null;
        }
        return new RoomShortResponse(room.getId(), room.getNumber(), room.isAvailable(), room.getTimesBooked());
    }

    private List<RoomShortResponse> toShortList(List<Room> rooms) {
        return rooms == null ? List.of() : rooms.stream().map(this::toShort).toList();
    }
}
