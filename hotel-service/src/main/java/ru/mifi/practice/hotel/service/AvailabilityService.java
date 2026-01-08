package ru.mifi.practice.hotel.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.mifi.practice.hotel.dto.AvailabilityRequest;
import ru.mifi.practice.hotel.dto.AvailabilityResponse;
import ru.mifi.practice.hotel.model.ReservationStatus;
import ru.mifi.practice.hotel.model.Room;
import ru.mifi.practice.hotel.model.RoomReservation;
import ru.mifi.practice.hotel.repository.RoomRepository;
import ru.mifi.practice.hotel.repository.RoomReservationRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final RoomRepository roomRepository;
    private final RoomReservationRepository reservationRepository;

    @Transactional
    public AvailabilityResponse confirmAvailability(Long roomId, AvailabilityRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        RoomReservation existing = reservationRepository.findByRequestId(request.requestId()).orElse(null);
        if (existing != null) {
            return new AvailabilityResponse(true, existing.getRequestId());
        }

        validateDates(request.startDate(), request.endDate());
        List<RoomReservation> overlaps = reservationRepository.findOverlaps(roomId, request.startDate(), request.endDate());
        if (!overlaps.isEmpty()) {
            log.warn("Room {} unavailable for {}-{}, overlaps {}", roomId, request.startDate(), request.endDate(), overlaps.size());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is not available for the selected dates");
        }

        RoomReservation reservation = RoomReservation.builder()
                .room(room)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .requestId(request.requestId())
                .status(ReservationStatus.HELD)
                .build();
        reservationRepository.save(reservation);
        room.setTimesBooked(room.getTimesBooked() + 1);
        roomRepository.save(room);

        log.info("Room {} held for request {} from {} to {}", roomId, request.requestId(), request.startDate(), request.endDate());
        return new AvailabilityResponse(true, request.requestId());
    }

    @Transactional
    public AvailabilityResponse release(String requestId) {
        RoomReservation reservation = reservationRepository.findByRequestId(requestId).orElse(null);
        if (reservation != null) {
            reservationRepository.delete(reservation);
            log.info("Released reservation {}", requestId);
        } else {
            log.info("Release request {} ignored - not found", requestId);
        }
        return new AvailabilityResponse(true, requestId);
    }

    private void validateDates(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date range");
        }
    }
}
