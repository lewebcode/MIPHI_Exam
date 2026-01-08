package ru.mifi.practice.booking.client.dto;

import java.time.LocalDate;

public record AvailabilityRequest(LocalDate startDate, LocalDate endDate, String requestId) {
}
