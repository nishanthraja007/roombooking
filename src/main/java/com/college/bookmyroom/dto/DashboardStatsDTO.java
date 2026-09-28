package com.college.bookmyroom.dto;

public class DashboardStatsDTO {

    private long totalRooms;
    private long totalGuests;
    private long totalBookings;
    private long confirmedBookings;
    private long cancelledBookings;

    public DashboardStatsDTO() {
    }

    public DashboardStatsDTO(long totalRooms, long totalGuests, long totalBookings, long confirmedBookings, long cancelledBookings) {
        this.totalRooms = totalRooms;
        this.totalGuests = totalGuests;
        this.totalBookings = totalBookings;
        this.confirmedBookings = confirmedBookings;
        this.cancelledBookings = cancelledBookings;
    }

    public long getTotalRooms() {
        return totalRooms;
    }

    public void setTotalRooms(long totalRooms) {
        this.totalRooms = totalRooms;
    }

    public long getTotalGuests() {
        return totalGuests;
    }

    public void setTotalGuests(long totalGuests) {
        this.totalGuests = totalGuests;
    }

    public long getTotalBookings() {
        return totalBookings;
    }

    public void setTotalBookings(long totalBookings) {
        this.totalBookings = totalBookings;
    }

    public long getConfirmedBookings() {
        return confirmedBookings;
    }

    public void setConfirmedBookings(long confirmedBookings) {
        this.confirmedBookings = confirmedBookings;
    }

    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(long cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }
}
