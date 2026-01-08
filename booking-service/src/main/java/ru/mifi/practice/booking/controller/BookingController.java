package ru.mifi.practice.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.mifi.practice.booking.dto.BookingRequest;
import ru.mifi.practice.booking.dto.BookingResponse;
import ru.mifi.practice.booking.service.BookingWorkflowService;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class BookingController {

    private final BookingWorkflowService bookingService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/booking")
    public BookingResponse create(@Valid @RequestBody BookingRequest request, Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        return bookingService.createBooking(request, token.getName(), token.getToken().getTokenValue());
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/bookings")
    public Page<BookingResponse> list(Authentication authentication,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        boolean isAdmin = token.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return bookingService.list(token.getName(), isAdmin, pageable);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/booking/{id}")
    public BookingResponse getById(@PathVariable Long id, Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        boolean isAdmin = token.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return bookingService.getById(id, token.getName(), isAdmin);
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @DeleteMapping("/booking/{id}")
    public void cancel(@PathVariable Long id, Authentication authentication) {
        JwtAuthenticationToken token = (JwtAuthenticationToken) authentication;
        boolean isAdmin = token.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        bookingService.cancel(id, token.getName(), isAdmin, token.getToken().getTokenValue());
    }
}
