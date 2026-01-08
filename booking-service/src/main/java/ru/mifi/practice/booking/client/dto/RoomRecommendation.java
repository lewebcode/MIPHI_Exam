package ru.mifi.practice.booking.client.dto;

public record RoomRecommendation(Long id, Long hotelId, String number, boolean available, int timesBooked) {
}
