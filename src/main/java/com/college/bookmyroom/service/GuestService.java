package com.college.bookmyroom.service;

import com.college.bookmyroom.dto.GuestDTO;
import com.college.bookmyroom.entity.Guest;
import com.college.bookmyroom.exception.GuestNotFoundException;
import com.college.bookmyroom.repository.GuestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GuestService {

    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Transactional
    public GuestDTO createGuest(GuestDTO dto) {
        Guest guest = new Guest();
        if (dto.getId() != null) {
            guest.setId(dto.getId());
        }
        guest.setName(dto.getName());
        guest.setPhone(dto.getPhone());

        Guest savedGuest = guestRepository.save(guest);
        return mapToDTO(savedGuest);
    }

    @Transactional(readOnly = true)
    public List<GuestDTO> getAllGuests() {
        return guestRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GuestDTO getGuestById(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new GuestNotFoundException("Guest not found with ID: " + id));
        return mapToDTO(guest);
    }

    private GuestDTO mapToDTO(Guest guest) {
        return new GuestDTO(guest.getId(), guest.getName(), guest.getPhone());
    }
}
