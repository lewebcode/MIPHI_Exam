package ru.mifi.practice.hotel.dto;

public record RoomResponse(Long id, Long hotelId, String number, boolean available, int timesBooked) {
}
