package com.college.bookmyroom.service;

import com.college.bookmyroom.dto.BookingResponseDTO;
import com.college.bookmyroom.dto.CreateBookingRequest;
import com.college.bookmyroom.dto.DashboardStatsDTO;
import com.college.bookmyroom.entity.Booking;
import com.college.bookmyroom.entity.BookingStatus;
import com.college.bookmyroom.entity.Guest;
import com.college.bookmyroom.entity.Room;
import com.college.bookmyroom.exception.BookingNotFoundException;
import com.college.bookmyroom.exception.GuestNotFoundException;
import com.college.bookmyroom.exception.InvalidDateRangeException;
import com.college.bookmyroom.exception.RoomNotAvailableException;
import com.college.bookmyroom.exception.RoomNotFoundException;
import com.college.bookmyroom.repository.BookingRepository;
import com.college.bookmyroom.repository.GuestRepository;
import com.college.bookmyroom.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, GuestRepository guestRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
    }

    @Transactional
    public BookingResponseDTO createBooking(CreateBookingRequest request) {
        LocalDate fromDate = request.getFromDate();
        LocalDate toDate = request.getToDate();

        if (fromDate == null || toDate == null || !fromDate.isBefore(toDate)) {
            throw new InvalidDateRangeException("fromDate must be strictly before toDate");
        }

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException("Room not found with ID: " + request.getRoomId()));

        Guest guest = guestRepository.findById(request.getGuestId())
                .orElseThrow(() -> new GuestNotFoundException("Guest not found with ID: " + request.getGuestId()));

        // Check date overlap against existing CONFIRMED bookings
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                room.getId(), fromDate, toDate, BookingStatus.CONFIRMED);

        if (!conflicts.isEmpty()) {
            throw new RoomNotAvailableException("Room " + room.getId() + " is already booked for the selected date range.");
        }

        long nights = ChronoUnit.DAYS.between(fromDate, toDate);
        BigDecimal totalCost = room.getNightlyRate().multiply(BigDecimal.valueOf(nights));

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setGuest(guest);
        booking.setFromDate(fromDate);
        booking.setToDate(toDate);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalCost(totalCost);

        Booking savedBooking = bookingRepository.save(booking);
        return mapToDTO(savedBooking);
    }

    @Transactional
    public BookingResponseDTO cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + bookingId));

        booking.setStatus(BookingStatus.CANCELLED);
        Booking updatedBooking = bookingRepository.save(booking);
        return mapToDTO(updatedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingResponseDTO getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));
        return mapToDTO(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getBookingsForMonth(int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        List<Booking> bookings = bookingRepository.findBookingsForMonthRange(startOfMonth, endOfMonth);
        return bookings.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        long totalRooms = roomRepository.count();
        long totalGuests = guestRepository.count();
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        long cancelledBookings = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        return new DashboardStatsDTO(totalRooms, totalGuests, totalBookings, confirmedBookings, cancelledBookings);
    }

    private BookingResponseDTO mapToDTO(Booking booking) {
        return new BookingResponseDTO(
                booking.getId(),
                booking.getRoom().getId(),
                booking.getRoom().getType(),
                booking.getGuest().getId(),
                booking.getGuest().getName(),
                booking.getFromDate(),
                booking.getToDate(),
                booking.getStatus(),
                booking.getTotalCost()
        );
    }
}
