package com.app.bookamenities.repository;

import com.app.bookamenities.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdAndBookingDate(Long userId, String bookingDate);

    List<Booking> findByBookingDateAndAmenityName(String date, String amenityName);

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.bookingDate < :today ORDER BY b.bookingDate DESC")
    List<Booking> findPastBookings(@Param("userId") Long userId, @Param("today") String today);

    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.bookingDate >= :today ORDER BY b.bookingDate ASC")
    List<Booking> findUpcomingBookings(@Param("userId") Long userId, @Param("today") String today);


}

