package com.app.bookamenities.service;

import com.app.bookamenities.dto.BookingRequest;
import com.app.bookamenities.entity.Booking;
import com.app.bookamenities.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
public class BookAmenitiesChatTool {

    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    public BookAmenitiesChatTool(BookingRepository bookingRepository, BookingService bookingService) {
        this.bookingRepository = bookingRepository;
        this.bookingService = bookingService;
    }

    @Tool(name = "get_past_bookings", description = "Fetch all completed or expired bookings for a given user ID")
    public List<Booking> getPastBookings(@ToolParam(
            description = "Required user ID. Tool should not be called without this value") Long userId) {
        LocalDate today = LocalDate.now();
        return bookingRepository.findPastBookings(userId, today);
    }

    @Tool(name = "get_upcoming_bookings", description = "Fetch all upcoming future bookings for a given user ID")
    public List<Booking> getUpcomingBookings(@ToolParam(
            description = "Required user ID. Tool should not be called without this value") Long userId) {
        LocalDate today = LocalDate.now();
        log.info("Fetching upcoming bookings");
        return bookingRepository.findUpcomingBookings(userId, today);
    }

    @Tool(name = "create_booking", description = "Create a new amenity booking for a user")
    public Booking createBooking(
            @ToolParam(description = "User ID") Long userId,
            @ToolParam(description = "Amenity name like badminton, pool") String amenity,
            @ToolParam(description = "Booking date") LocalDate date,
            @ToolParam(description = "Start time") LocalTime startTime,
            @ToolParam(description = "End time ") LocalTime endTime
    ) {
        BookingRequest bookingRequest = new BookingRequest();
        bookingRequest.setUserId(userId);
        bookingRequest.setAmenityName(amenity);
        bookingRequest.setBookingDate(date);
        bookingRequest.setStartTime(startTime);
        bookingRequest.setEndTime(endTime);
        return bookingService.bookSlot(bookingRequest);
    }

    @Tool(
            name = "cancel_booking",
            description = """
    Cancel a booking using the booking ID.
    
    IMPORTANT:
    - Use this tool ONLY when user explicitly provides a booking ID.
    - Extract numeric booking ID from the user query.
    - Do not call get_upcoming_bookings first if booking ID is already present.
    """
    )
    public void cancelBooking(
            @ToolParam(description = "User ID") Long userId,
            @ToolParam(description = "Booking ID") Long bookingId) {
        bookingService.deleteBooking(bookingId, userId);
    }
}
