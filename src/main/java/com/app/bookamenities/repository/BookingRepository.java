package com.app.bookamenities.repository;

import com.app.bookamenities.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUser_UserIdAndBookingDate(Long userId, LocalDate bookingDate);

    List<Booking> findByBookingDateAndAmenityName(LocalDate bookingDate, String amenityName);

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId AND b.bookingDate < :today ORDER BY b.bookingDate DESC")
    List<Booking> findPastBookings(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("SELECT b FROM Booking b WHERE b.user.userId = :userId AND b.bookingDate >= :today ORDER BY b.bookingDate ASC")
    List<Booking> findUpcomingBookings(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.user.userId = :userId AND b.amenityName = :amenityName AND YEAR(b.bookingDate) = :year")
    int countBookingsForYear(@Param("userId") Long userId,
                             @Param("amenityName") String amenityName,
                             @Param("year") int year);

    @Query(value = "SELECT COUNT(*) FROM booking b " +
            "WHERE b.user_id = :userId " +
            "AND b.amenity_name = :amenityName " +
            "AND WEEK(b.booking_date) = :currentWeek " +
            "AND YEAR(b.booking_date) = :currentYear",
            nativeQuery = true)
    int countWeeklyBookings(
            @Param("userId") Long userId,
            @Param("amenityName") String amenityName,
            @Param("currentWeek") int currentWeek,
            @Param("currentYear") int currentYear);

        @Query("""
        SELECT COUNT(b)
        FROM Booking b
        WHERE b.user.userId = :userId
          AND b.amenityName = :amenityName
          AND MONTH(b.bookingDate) = :currentMonth
          AND YEAR(b.bookingDate) = :currentYear
    """)
        int countMonthlyBookings(@Param("userId") Long userId,
                                 @Param("amenityName") String amenityName,
                                 @Param("currentMonth") int currentMonth,
                                 @Param("currentYear") int currentYear);
    

}


