package ru.mifi.practice.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.mifi.practice.booking.client.HotelClient;
import ru.mifi.practice.booking.client.dto.RoomRecommendation;
import ru.mifi.practice.booking.dto.BookingRequest;
import ru.mifi.practice.booking.dto.BookingResponse;
import ru.mifi.practice.booking.mapper.BookingMapper;
import ru.mifi.practice.booking.model.Booking;
import ru.mifi.practice.booking.model.BookingStatus;
import ru.mifi.practice.booking.model.User;
import ru.mifi.practice.booking.repository.BookingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingWorkflowService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final HotelClient hotelClient;
    private final BookingMapper mapper;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username, String token) {
        User user = userService.getByUsername(username);
        validateDates(request.startDate(), request.endDate());
        String requestId = Optional.ofNullable(request.requestId()).orElse(UUID.randomUUID().toString());
        Booking existing = bookingRepository.findByRequestId(requestId).orElse(null);
        if (existing != null) {
            log.info("Idempotent request detected: requestId={}, bookingId={}", requestId, existing.getId());
            return mapper.toResponse(existing);
        }

        Long roomId = resolveRoom(request, token);
        Booking booking = Booking.builder()
                .user(user)
                .roomId(roomId)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .status(BookingStatus.PENDING)
                .requestId(requestId)
                .build();
        bookingRepository.save(booking);
        log.info("Booking created: bookingId={}, requestId={}, roomId={}, status=PENDING, user={}", 
                booking.getId(), requestId, roomId, username);

        try {
            log.info("Requesting availability confirmation: bookingId={}, requestId={}, roomId={}", 
                    booking.getId(), requestId, roomId);
            hotelClient.confirmAvailability(roomId, request.startDate(), request.endDate(), requestId, token);
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            log.info("Booking confirmed: bookingId={}, requestId={}, status=CONFIRMED, user={}", 
                    booking.getId(), requestId, username);
        } catch (Exception ex) {
            log.warn("Booking confirmation failed: bookingId={}, requestId={}, error={}", 
                    booking.getId(), requestId, ex.getMessage());
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            log.info("Compensating: releasing room reservation: bookingId={}, requestId={}, roomId={}", 
                    booking.getId(), requestId, roomId);
            hotelClient.release(roomId, requestId, token);
            log.info("Compensation completed: bookingId={}, requestId={}, status=CANCELLED", 
                    booking.getId(), requestId);
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to confirm room availability");
        }

        return mapper.toResponse(booking);
    }

    @Transactional(readOnly = true)
    public Page<BookingResponse> list(String username, boolean isAdmin, Pageable pageable) {
        if (isAdmin) {
            return bookingRepository.findAll(pageable).map(mapper::toResponse);
        }
        User user = userService.getByUsername(username);
        return bookingRepository.findByUser(user, pageable).map(mapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long id, String username, boolean isAdmin) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!isAdmin && !booking.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return mapper.toResponse(booking);
    }

    @Transactional
    public void cancel(Long id, String username, boolean isAdmin, String token) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!isAdmin && !booking.getUser().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        hotelClient.release(booking.getRoomId(), booking.getRequestId(), token);
    }

    private Long resolveRoom(BookingRequest request, String token) {
        if (!request.autoSelect()) {
            if (request.roomId() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roomId is required when autoSelect=false");
            }
            return request.roomId();
        }
        List<Long> recommended = hotelClient.recommend(token).stream().map(RoomRecommendation::id).toList();
        if (recommended.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No rooms available");
        }
        return recommended.get(0);
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range");
        }
    }

}
