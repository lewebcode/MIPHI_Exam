package ru.mifi.practice.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoomRequest(
        @NotNull Long hotelId,
        @NotBlank String number,
        boolean available
) {
}
