package com.college.bookmyroom.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateBookingRequest {

    @NotNull(message = "roomId is required")
    private Long roomId;

    @NotNull(message = "guestId is required")
    private Long guestId;

    @NotNull(message = "fromDate is required")
    private LocalDate fromDate;

    @NotNull(message = "toDate is required")
    private LocalDate toDate;

    public CreateBookingRequest() {
    }

    public CreateBookingRequest(Long roomId, Long guestId, LocalDate fromDate, LocalDate toDate) {
        this.roomId = roomId;
        this.guestId = guestId;
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }
}
