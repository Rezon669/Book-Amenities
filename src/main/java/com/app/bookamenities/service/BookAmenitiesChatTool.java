package com.app.bookamenities.service;

import com.app.bookamenities.entity.Booking;
import com.app.bookamenities.repository.BookingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class BookAmenitiesChatTool {

    private final BookingRepository bookingRepository;

    public BookAmenitiesChatTool(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
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
}
