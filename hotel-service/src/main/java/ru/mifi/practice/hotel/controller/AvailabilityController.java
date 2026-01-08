package ru.mifi.practice.hotel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mifi.practice.hotel.dto.AvailabilityRequest;
import ru.mifi.practice.hotel.dto.AvailabilityResponse;
import ru.mifi.practice.hotel.service.AvailabilityService;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{id}/confirm-availability")
    public AvailabilityResponse confirm(@PathVariable("id") Long roomId,
                                        @Valid @RequestBody AvailabilityRequest request) {
        return availabilityService.confirmAvailability(roomId, request);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/{id}/release")
    public AvailabilityResponse release(@PathVariable("id") Long roomId,
                                        @Valid @RequestBody AvailabilityRequest request) {
        return availabilityService.release(request.requestId());
    }
}
