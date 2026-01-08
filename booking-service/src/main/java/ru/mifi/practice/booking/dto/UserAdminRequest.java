package ru.mifi.practice.booking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.mifi.practice.booking.model.Role;

public record UserAdminRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotNull Role role
) {
}
