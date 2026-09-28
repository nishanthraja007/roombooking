package com.college.bookmyroom.config;

import com.college.bookmyroom.entity.Guest;
import com.college.bookmyroom.entity.Room;
import com.college.bookmyroom.repository.GuestRepository;
import com.college.bookmyroom.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;

    public DataInitializer(RoomRepository roomRepository, GuestRepository guestRepository) {
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
    }

    @Override
    public void run(String... args) {
        if (roomRepository.count() == 0) {
            roomRepository.save(new Room("Deluxe Suite", 2, new BigDecimal("2500.00")));
            roomRepository.save(new Room("Executive Standard", 2, new BigDecimal("1800.00")));
            roomRepository.save(new Room("VIP Family Suite", 4, new BigDecimal("4000.00")));
            roomRepository.save(new Room("Single Study Room", 1, new BigDecimal("1200.00")));
        }

        if (guestRepository.count() == 0) {
            guestRepository.save(new Guest("Prof. Rajesh Kumar", "+91 9876543210"));
            guestRepository.save(new Guest("Dr. Anita Sharma", "+91 9123456789"));
            guestRepository.save(new Guest("Dr. Vikram Verma", "+91 9988776655"));
        }
    }
}
