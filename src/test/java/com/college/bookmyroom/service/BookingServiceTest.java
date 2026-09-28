package com.college.bookmyroom.service;

import com.college.bookmyroom.dto.BookingResponseDTO;
import com.college.bookmyroom.dto.CreateBookingRequest;
import com.college.bookmyroom.entity.Booking;
import com.college.bookmyroom.entity.BookingStatus;
import com.college.bookmyroom.entity.Guest;
import com.college.bookmyroom.entity.Room;
import com.college.bookmyroom.exception.InvalidDateRangeException;
import com.college.bookmyroom.exception.RoomNotAvailableException;
import com.college.bookmyroom.repository.BookingRepository;
import com.college.bookmyroom.repository.GuestRepository;
import com.college.bookmyroom.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private GuestRepository guestRepository;

    @InjectMocks
    private BookingService bookingService;

    private Room sampleRoom;
    private Guest sampleGuest;

    @BeforeEach
    void setUp() {
        sampleRoom = new Room(101L, "Deluxe", 2, new BigDecimal("2500.00"));
        sampleGuest = new Guest(5L, "Prof. Smith", "+91 9876543210");
    }

    @Test
    @DisplayName("Should successfully create booking and calculate correct total cost")
    void testCreateBookingSuccess() {
        LocalDate from = LocalDate.of(2026, 10, 10);
        LocalDate to = LocalDate.of(2026, 10, 13); // 3 nights

        CreateBookingRequest request = new CreateBookingRequest(101L, 5L, from, to);

        when(roomRepository.findById(101L)).thenReturn(Optional.of(sampleRoom));
        when(guestRepository.findById(5L)).thenReturn(Optional.of(sampleGuest));
        when(bookingRepository.findConflictingBookings(eq(101L), eq(from), eq(to), eq(BookingStatus.CONFIRMED)))
                .thenReturn(Collections.emptyList());

        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking b = invocation.getArgument(0);
            b.setId(1L);
            return b;
        });

        BookingResponseDTO response = bookingService.createBooking(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(101L, response.getRoomId());
        assertEquals(5L, response.getGuestId());
        assertEquals(BookingStatus.CONFIRMED, response.getStatus());
        assertEquals(new BigDecimal("7500.00"), response.getTotalCost()); // 3 * 2500
    }

    @Test
    @DisplayName("Should reject booking creation when date overlap conflict exists (Rule 1 & Rule 3)")
    void testCreateBookingConflict() {
        LocalDate from = LocalDate.of(2026, 10, 10);
        LocalDate to = LocalDate.of(2026, 10, 15);

        CreateBookingRequest request = new CreateBookingRequest(101L, 5L, from, to);

        Booking existing = new Booking(101L, sampleRoom, sampleGuest, from, to, BookingStatus.CONFIRMED, new BigDecimal("12500.00"));

        when(roomRepository.findById(101L)).thenReturn(Optional.of(sampleRoom));
        when(guestRepository.findById(5L)).thenReturn(Optional.of(sampleGuest));
        when(bookingRepository.findConflictingBookings(eq(101L), eq(from), eq(to), eq(BookingStatus.CONFIRMED)))
                .thenReturn(List.of(existing));

        RoomNotAvailableException ex = assertThrows(RoomNotAvailableException.class, () -> bookingService.createBooking(request));
        assertTrue(ex.getMessage().contains("Room 101 is already booked"));
        verify(bookingRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject invalid date range where fromDate is on or after toDate")
    void testInvalidDateRange() {
        LocalDate from = LocalDate.of(2026, 10, 15);
        LocalDate to = LocalDate.of(2026, 10, 10);

        CreateBookingRequest request = new CreateBookingRequest(101L, 5L, from, to);

        assertThrows(InvalidDateRangeException.class, () -> bookingService.createBooking(request));
    }

    @Test
    @DisplayName("Should cancel an existing booking and change status to CANCELLED (Rule 4)")
    void testCancelBooking() {
        LocalDate from = LocalDate.of(2026, 10, 10);
        LocalDate to = LocalDate.of(2026, 10, 13);
        Booking booking = new Booking(1L, sampleRoom, sampleGuest, from, to, BookingStatus.CONFIRMED, new BigDecimal("7500.00"));

        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookingResponseDTO response = bookingService.cancelBooking(1L);

        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        verify(bookingRepository).save(booking);
    }
}
