package com.college.bookmyroom.controller;

import com.college.bookmyroom.dto.CreateBookingRequest;
import com.college.bookmyroom.dto.GuestDTO;
import com.college.bookmyroom.dto.RoomDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Full End-To-End Flow: Create Room, Guest, Booking, Overlap Check (409), Cancel & Monthly View")
    void testFullBookingWorkflow() throws Exception {
        // 1. Create Room
        RoomDTO roomDTO = new RoomDTO(null, "Super Deluxe", 3, new BigDecimal("3000.00"));
        String roomResponse = mockMvc.perform(post("/api/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roomDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.type", is("Super Deluxe")))
                .andReturn().getResponse().getContentAsString();

        Long roomId = objectMapper.readTree(roomResponse).get("id").asLong();

        // 2. Create Guest
        GuestDTO guestDTO = new GuestDTO(null, "Dr. Alice Vance", "+91 9999988888");
        String guestResponse = mockMvc.perform(post("/api/guests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Dr. Alice Vance")))
                .andReturn().getResponse().getContentAsString();

        Long guestId = objectMapper.readTree(guestResponse).get("id").asLong();

        // 3. Create Booking for 2026-10-10 to 2026-10-14 (4 nights @ 3000 = 12000)
        CreateBookingRequest request1 = new CreateBookingRequest(
                roomId, guestId, LocalDate.of(2026, 10, 10), LocalDate.of(2026, 10, 14));

        String booking1Response = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CONFIRMED")))
                .andExpect(jsonPath("$.totalCost", is(12000.00)))
                .andReturn().getResponse().getContentAsString();

        Long bookingId = objectMapper.readTree(booking1Response).get("id").asLong();

        // 4. Overlapping booking attempt (2026-10-12 to 2026-10-16) -> Expect 409 Conflict
        CreateBookingRequest requestOverlap = new CreateBookingRequest(
                roomId, guestId, LocalDate.of(2026, 10, 12), LocalDate.of(2026, 10, 16));

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestOverlap)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.error", is("Room Not Available")))
                .andExpect(jsonPath("$.message", containsString("is already booked for the selected date range")));

        // 5. Cancel Booking
        mockMvc.perform(put("/api/bookings/" + bookingId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELLED")));

        // 6. Overlapping request after cancellation SHOULD NOW SUCCEED
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestOverlap)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("CONFIRMED")));

        // 7. Get monthly bookings for October 2026
        mockMvc.perform(get("/api/bookings/month")
                        .param("year", "2026")
                        .param("month", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }
}
