package ru.mifi.practice.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import ru.mifi.practice.booking.dto.AuthRequest;
import ru.mifi.practice.booking.dto.AuthResponse;
import ru.mifi.practice.booking.service.UserService;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/user/register")
    public AuthResponse register(@Valid @RequestBody AuthRequest request) {
        return userService.register(request);
    }

    @PostMapping("/user/auth")
    public AuthResponse authenticate(@Valid @RequestBody AuthRequest request) {
        return userService.authenticate(request);
    }
}
