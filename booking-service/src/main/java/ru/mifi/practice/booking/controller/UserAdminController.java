package ru.mifi.practice.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mifi.practice.booking.dto.UserAdminRequest;
import ru.mifi.practice.booking.model.User;
import ru.mifi.practice.booking.service.UserService;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public User create(@Valid @RequestBody UserAdminRequest request) {
        return userService.create(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping
    public User update(@Valid @RequestBody UserAdminRequest request) {
        return userService.update(request);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping
    public void delete(@RequestParam String username) {
        userService.delete(username);
    }
}
