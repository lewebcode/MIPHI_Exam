package ru.mifi.practice.hotel.dto;

public record RoomShortResponse(Long id, String number, boolean available, int timesBooked) {
}
