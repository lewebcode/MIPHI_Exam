package ru.mifi.practice.hotel.dto;

import jakarta.validation.constraints.NotBlank;

public record HotelRequest(
        @NotBlank String name,
        @NotBlank String address
) {
}
