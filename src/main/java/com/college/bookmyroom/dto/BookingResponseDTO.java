package com.college.bookmyroom.dto;

import com.college.bookmyroom.entity.BookingStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public class BookingResponseDTO {

    private Long id;
    private Long roomId;
    private String roomType;
    private Long guestId;
    private String guestName;
    private LocalDate fromDate;
    private LocalDate toDate;
    private BookingStatus status;
    private BigDecimal totalCost;

    public BookingResponseDTO() {
    }

    public BookingResponseDTO(Long id, Long roomId, String roomType, Long guestId, String guestName,
                              LocalDate fromDate, LocalDate toDate, BookingStatus status, BigDecimal totalCost) {
        this.id = id;
        this.roomId = roomId;
        this.roomType = roomType;
        this.guestId = guestId;
        this.guestName = guestName;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.status = status;
        this.totalCost = totalCost;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public Long getGuestId() {
        return guestId;
    }

    public void setGuestId(Long guestId) {
        this.guestId = guestId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
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

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
}
