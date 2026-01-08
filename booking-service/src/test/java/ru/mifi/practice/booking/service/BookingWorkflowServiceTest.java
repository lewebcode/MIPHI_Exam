package ru.mifi.practice.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import ru.mifi.practice.booking.client.HotelClient;
import ru.mifi.practice.booking.client.dto.AvailabilityResponse;
import ru.mifi.practice.booking.client.dto.RoomRecommendation;
import ru.mifi.practice.booking.dto.BookingRequest;
import ru.mifi.practice.booking.dto.BookingResponse;
import ru.mifi.practice.booking.mapper.BookingMapper;
import ru.mifi.practice.booking.model.Booking;
import ru.mifi.practice.booking.model.BookingStatus;
import ru.mifi.practice.booking.model.Role;
import ru.mifi.practice.booking.model.User;
import ru.mifi.practice.booking.repository.BookingRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class BookingWorkflowServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Mock
    private HotelClient hotelClient;

    @Mock
    private BookingMapper mapper;

    @InjectMocks
    private BookingWorkflowService bookingService;

    private User testUser;
    private BookingRequest validRequest;
    private Booking savedBooking;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(Role.USER);

        validRequest = new BookingRequest(
                1L,
                false,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                null
        );

        savedBooking = Booking.builder()
                .id(1L)
                .user(testUser)
                .roomId(1L)
                .startDate(validRequest.startDate())
                .endDate(validRequest.endDate())
                .status(BookingStatus.PENDING)
                .requestId("test-request-id")
                .build();
    }

    @Test
    void testCreateBooking_Success() {
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        when(userService.getByUsername("testuser")).thenReturn(testUser);
        when(bookingRepository.findByRequestId(any())).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            if (b.getId() == null) {
                b.setId(1L);
            }
            return b;
        });
        when(hotelClient.confirmAvailability(eq(1L), any(), any(), any(), any()))
                .thenReturn(new AvailabilityResponse(true, "test-request-id"));
        when(mapper.toResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            return new BookingResponse(b.getId(), b.getRoomId(), b.getStartDate(), 
                    b.getEndDate(), b.getStatus(), b.getCreatedAt(), b.getRequestId());
        });

        BookingResponse response = bookingService.createBooking(validRequest, "testuser", "token");

        assertNotNull(response);
        verify(bookingRepository, times(2)).save(bookingCaptor.capture());
        verify(hotelClient).confirmAvailability(eq(1L), any(), any(), any(), any());
        List<Booking> savedBookings = bookingCaptor.getAllValues();
        assertEquals(BookingStatus.CONFIRMED, savedBookings.get(1).getStatus());
    }

    @Test
    void testCreateBooking_HotelServiceFailure_Compensation() {
        ArgumentCaptor<Booking> bookingCaptor = ArgumentCaptor.forClass(Booking.class);
        when(userService.getByUsername("testuser")).thenReturn(testUser);
        when(bookingRepository.findByRequestId(any())).thenReturn(Optional.empty());
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            if (b.getId() == null) {
                b.setId(1L);
            }
            return b;
        });
        when(hotelClient.confirmAvailability(eq(1L), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Service unavailable"));

        assertThrows(ResponseStatusException.class, () -> {
            bookingService.createBooking(validRequest, "testuser", "token");
        });

        verify(bookingRepository, times(2)).save(bookingCaptor.capture());
        verify(hotelClient).release(eq(1L), any(), eq("token"));
        List<Booking> savedBookings = bookingCaptor.getAllValues();
        assertEquals(BookingStatus.CANCELLED, savedBookings.get(1).getStatus());
    }

    @Test
    void testCreateBooking_Idempotency() {
        Booking existingBooking = Booking.builder()
                .id(2L)
                .user(testUser)
                .roomId(1L)
                .status(BookingStatus.CONFIRMED)
                .requestId("existing-request-id")
                .build();

        when(bookingRepository.findByRequestId("existing-request-id"))
                .thenReturn(Optional.of(existingBooking));
        when(mapper.toResponse(existingBooking)).thenReturn(
                new BookingResponse(2L, 1L, existingBooking.getStartDate(), existingBooking.getEndDate(), 
                        BookingStatus.CONFIRMED, existingBooking.getCreatedAt(), "existing-request-id"));

        BookingRequest requestWithId = new BookingRequest(
                1L,
                false,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                "existing-request-id"
        );

        BookingResponse response = bookingService.createBooking(requestWithId, "testuser", "token");

        assertNotNull(response);
        verify(bookingRepository, never()).save(any(Booking.class));
        verify(hotelClient, never()).confirmAvailability(any(), any(), any(), any(), any());
    }

    @Test
    void testCreateBooking_AutoSelect() {
        when(userService.getByUsername("testuser")).thenReturn(testUser);
        when(bookingRepository.findByRequestId(any())).thenReturn(Optional.empty());
        when(hotelClient.recommend("token")).thenReturn(
                List.of(new RoomRecommendation(5L, 1L, "101", true, 0)));
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(hotelClient.confirmAvailability(eq(5L), any(), any(), any(), any()))
                .thenReturn(new AvailabilityResponse(true, "test-request-id"));
        when(mapper.toResponse(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            return new BookingResponse(b.getId(), b.getRoomId(), b.getStartDate(), 
                    b.getEndDate(), b.getStatus(), b.getCreatedAt(), b.getRequestId());
        });

        BookingRequest autoRequest = new BookingRequest(
                null,
                true,
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(3),
                null
        );

        BookingResponse response = bookingService.createBooking(autoRequest, "testuser", "token");

        assertNotNull(response);
        verify(hotelClient).recommend("token");
        verify(hotelClient).confirmAvailability(eq(5L), any(), any(), any(), any());
    }

    @Test
    void testCreateBooking_InvalidDates() {
        BookingRequest invalidRequest = new BookingRequest(
                1L,
                false,
                LocalDate.now().plusDays(3),
                LocalDate.now().plusDays(1),
                null
        );

        assertThrows(ResponseStatusException.class, () -> {
            bookingService.createBooking(invalidRequest, "testuser", "token");
        });
    }
}
