package ru.mifi.practice.hotel.dto;

public record RoomStatResponse(Long id, String number, boolean available, int timesBooked) {
}
