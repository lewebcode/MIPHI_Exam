package ru.mifi.practice.hotel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mifi.practice.hotel.dto.RoomRequest;
import ru.mifi.practice.hotel.dto.RoomResponse;
import ru.mifi.practice.hotel.dto.RoomStatResponse;
import ru.mifi.practice.hotel.mapper.RoomMapper;
import ru.mifi.practice.hotel.service.RoomCrudService;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomCrudService roomService;
    private final RoomMapper mapper;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public RoomResponse create(@Valid @RequestBody RoomRequest request) {
        return mapper.toResponse(roomService.create(request));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public List<RoomResponse> listAvailable() {
        return roomService.findAllAvailable().stream().map(mapper::toResponse).toList();
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/recommend")
    public List<RoomResponse> recommend() {
        return roomService.recommended().stream().map(mapper::toResponse).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public List<RoomStatResponse> stats(@RequestParam(required = false) Boolean available) {
        return roomService.stats(available).stream()
                .map(r -> new RoomStatResponse(r.getId(), r.getNumber(), r.isAvailable(), r.getTimesBooked()))
                .toList();
    }
}
