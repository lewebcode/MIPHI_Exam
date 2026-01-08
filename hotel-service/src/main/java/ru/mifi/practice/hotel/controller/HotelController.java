package ru.mifi.practice.hotel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mifi.practice.hotel.dto.HotelRequest;
import ru.mifi.practice.hotel.dto.HotelResponse;
import ru.mifi.practice.hotel.mapper.HotelMapper;
import ru.mifi.practice.hotel.service.HotelCrudService;

import java.util.List;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelCrudService hotelService;
    private final HotelMapper mapper;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public HotelResponse create(@Valid @RequestBody HotelRequest request) {
        return mapper.toResponse(hotelService.create(request));
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping
    public List<HotelResponse> list() {
        return hotelService.findAll().stream().map(mapper::toResponse).toList();
    }
}
