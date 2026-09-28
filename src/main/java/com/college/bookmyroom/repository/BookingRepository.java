package com.college.bookmyroom.repository;

import com.college.bookmyroom.entity.Booking;
import com.college.bookmyroom.entity.BookingStatus;
import com.college.bookmyroom.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.status = :status AND b.fromDate < :toDate AND b.toDate > :fromDate")
    List<Booking> findConflictingBookings(@Param("roomId") Long roomId,
                                         @Param("fromDate") LocalDate fromDate,
                                         @Param("toDate") LocalDate toDate,
                                         @Param("status") BookingStatus status);

    @Query("SELECT r FROM Room r WHERE r.id NOT IN (" +
           "SELECT b.room.id FROM Booking b WHERE b.status = :status AND b.fromDate < :toDate AND b.toDate > :fromDate)")
    List<Room> findAvailableRooms(@Param("fromDate") LocalDate fromDate,
                                  @Param("toDate") LocalDate toDate,
                                  @Param("status") BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE (b.fromDate <= :endOfMonth AND b.toDate >= :startOfMonth) ORDER BY b.fromDate ASC")
    List<Booking> findBookingsForMonthRange(@Param("startOfMonth") LocalDate startOfMonth,
                                            @Param("endOfMonth") LocalDate endOfMonth);

    long countByStatus(BookingStatus status);

    boolean existsByRoomIdAndStatus(Long roomId, BookingStatus status);

    void deleteByRoomId(Long roomId);
}
