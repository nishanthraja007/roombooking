package com.college.bookmyroom.service;

import com.college.bookmyroom.dto.RoomDTO;
import com.college.bookmyroom.entity.BookingStatus;
import com.college.bookmyroom.entity.Room;
import com.college.bookmyroom.exception.InvalidDateRangeException;
import com.college.bookmyroom.exception.RoomNotAvailableException;
import com.college.bookmyroom.exception.RoomNotFoundException;
import com.college.bookmyroom.repository.BookingRepository;
import com.college.bookmyroom.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public RoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public RoomDTO createRoom(RoomDTO dto) {
        Room room = new Room();
        if (dto.getId() != null) {
            room.setId(dto.getId());
        }
        room.setType(dto.getType());
        room.setCapacity(dto.getCapacity());
        room.setNightlyRate(dto.getNightlyRate());

        Room savedRoom = roomRepository.save(room);
        return mapToDTO(savedRoom);
    }

    @Transactional(readOnly = true)
    public List<RoomDTO> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoomDTO getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Room not found with ID: " + id));
        return mapToDTO(room);
    }

    @Transactional(readOnly = true)
    public List<RoomDTO> getAvailableRooms(LocalDate fromDate, LocalDate toDate) {
        if (fromDate == null || toDate == null || !fromDate.isBefore(toDate)) {
            throw new InvalidDateRangeException("fromDate must be strictly before toDate");
        }
        List<Room> availableRooms = bookingRepository.findAvailableRooms(fromDate, toDate, BookingStatus.CONFIRMED);
        return availableRooms.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public RoomDTO updateRoom(Long id, RoomDTO dto) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Room not found with ID: " + id));

        room.setType(dto.getType());
        room.setCapacity(dto.getCapacity());
        room.setNightlyRate(dto.getNightlyRate());

        Room updatedRoom = roomRepository.save(room);
        return mapToDTO(updatedRoom);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Room not found with ID: " + id));

        if (bookingRepository.existsByRoomIdAndStatus(id, BookingStatus.CONFIRMED)) {
            throw new RoomNotAvailableException("Cannot delete Room #" + id + " because it currently has active CONFIRMED bookings. Please cancel them first.");
        }

        bookingRepository.deleteByRoomId(id);
        roomRepository.delete(room);
    }

    private RoomDTO mapToDTO(Room room) {
        return new RoomDTO(room.getId(), room.getType(), room.getCapacity(), room.getNightlyRate());
    }
}
