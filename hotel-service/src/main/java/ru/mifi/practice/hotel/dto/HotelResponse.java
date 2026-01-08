package ru.mifi.practice.hotel.dto;

import java.util.List;

public record HotelResponse(Long id, String name, String address, List<RoomShortResponse> rooms) {
}
