package ru.mifi.practice.booking.dto;

import ru.mifi.practice.booking.model.BookingStatus;

import java.time.Instant;
import java.time.LocalDate;

public record BookingResponse(Long id,
                              Long roomId,
                              LocalDate startDate,
                              LocalDate endDate,
                              BookingStatus status,
                              Instant createdAt,
                              String requestId) {
}
