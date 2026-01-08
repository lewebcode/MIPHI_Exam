package ru.mifi.practice.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BookingRequest(
        Long roomId,
        boolean autoSelect,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        String requestId
) {
}
