package ru.mifi.practice.hotel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.mifi.practice.hotel.dto.AvailabilityRequest;
import ru.mifi.practice.hotel.dto.AvailabilityResponse;
import ru.mifi.practice.hotel.model.Hotel;
import ru.mifi.practice.hotel.model.ReservationStatus;
import ru.mifi.practice.hotel.model.Room;
import ru.mifi.practice.hotel.model.RoomReservation;
import ru.mifi.practice.hotel.repository.RoomRepository;
import ru.mifi.practice.hotel.repository.RoomReservationRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomReservationRepository reservationRepository;

    @InjectMocks
    private AvailabilityService availabilityService;

    private Room testRoom;
    private Hotel testHotel;
    private AvailabilityRequest validRequest;

    @BeforeEach
    void setUp() {
        testHotel = Hotel.builder()
                .id(1L)
                .name("Test Hotel")
                .address("Test Address")
                .build();

        testRoom = Room.builder()
                .id(1L)
                .hotel(testHotel)
                .number("101")
                .available(true)
                .timesBooked(0)
                .build();

        validRequest = new AvailabilityRequest(
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                "test-request-id"
        );
    }

    @Test
    void testConfirmAvailability_Success() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(reservationRepository.findByRequestId("test-request-id")).thenReturn(Optional.empty());
        when(reservationRepository.findOverlaps(eq(1L), any(), any())).thenReturn(Collections.emptyList());
        when(reservationRepository.save(any(RoomReservation.class))).thenAnswer(invocation -> {
            RoomReservation r = invocation.getArgument(0);
            r.setId(1L);
            return r;
        });
        when(roomRepository.save(any(Room.class))).thenReturn(testRoom);

        AvailabilityResponse response = availabilityService.confirmAvailability(1L, validRequest);

        assertNotNull(response);
        assertTrue(response.available());
        assertEquals("test-request-id", response.requestId());
        verify(reservationRepository).save(any(RoomReservation.class));
        verify(roomRepository).save(any(Room.class));
        assertEquals(1, testRoom.getTimesBooked());
    }

    @Test
    void testConfirmAvailability_Idempotency() {
        RoomReservation existing = RoomReservation.builder()
                .id(1L)
                .room(testRoom)
                .startDate(validRequest.startDate())
                .endDate(validRequest.endDate())
                .requestId("test-request-id")
                .status(ReservationStatus.HELD)
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(reservationRepository.findByRequestId("test-request-id")).thenReturn(Optional.of(existing));

        AvailabilityResponse response = availabilityService.confirmAvailability(1L, validRequest);

        assertNotNull(response);
        assertTrue(response.available());
        assertEquals("test-request-id", response.requestId());
        verify(reservationRepository, never()).save(any(RoomReservation.class));
    }

    @Test
    void testConfirmAvailability_OverlapConflict() {
        RoomReservation overlap = RoomReservation.builder()
                .id(1L)
                .room(testRoom)
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(2))
                .status(ReservationStatus.HELD)
                .build();

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(reservationRepository.findByRequestId("test-request-id")).thenReturn(Optional.empty());
        when(reservationRepository.findOverlaps(eq(1L), any(), any())).thenReturn(List.of(overlap));

        assertThrows(ResponseStatusException.class, () -> {
            availabilityService.confirmAvailability(1L, validRequest);
        });

        verify(reservationRepository, never()).save(any(RoomReservation.class));
    }

    @Test
    void testConfirmAvailability_RoomNotFound() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            availabilityService.confirmAvailability(1L, validRequest);
        });
    }

    @Test
    void testRelease_Success() {
        RoomReservation reservation = RoomReservation.builder()
                .id(1L)
                .room(testRoom)
                .requestId("test-request-id")
                .status(ReservationStatus.HELD)
                .build();

        when(reservationRepository.findByRequestId("test-request-id")).thenReturn(Optional.of(reservation));

        AvailabilityResponse response = availabilityService.release("test-request-id");

        assertNotNull(response);
        assertTrue(response.available());
        verify(reservationRepository).delete(reservation);
    }

    @Test
    void testRelease_NotFound() {
        when(reservationRepository.findByRequestId("non-existent")).thenReturn(Optional.empty());

        AvailabilityResponse response = availabilityService.release("non-existent");

        assertNotNull(response);
        assertTrue(response.available());
        verify(reservationRepository, never()).delete(any());
    }

    @Test
    void testConfirmAvailability_InvalidDates() {
        AvailabilityRequest invalidRequest = new AvailabilityRequest(
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(1),
                "test-request-id"
        );

        when(roomRepository.findById(1L)).thenReturn(Optional.of(testRoom));
        when(reservationRepository.findByRequestId("test-request-id")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> {
            availabilityService.confirmAvailability(1L, invalidRequest);
        });
    }
}
