package com.app.bookamenities.controller;

import com.app.bookamenities.dto.BookingRequest;
import com.app.bookamenities.entity.Booking;
import com.app.bookamenities.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/book-amenities/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody BookingRequest request) {
        Booking booking = bookingService.bookSlot(request);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/past/{userId}")
    public ResponseEntity<?> getPastBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getPastBookings(userId));
    }

    @GetMapping("/upcoming/{userId}")
    public ResponseEntity<?> getUpcomingBookings(@PathVariable Long userId) {
        return ResponseEntity.ok(bookingService.getUpcomingBookings(userId));
    }

    @DeleteMapping("/{bookingId}/user/{userId}")
    public ResponseEntity<?> deleteBooking(
            @PathVariable Long bookingId,
            @PathVariable Long userId) {
        bookingService.deleteBooking(bookingId, userId);
        HashMap<String, String> response = new HashMap<>();
        response.put("message", "Booking deleted successfully");

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{bookingId}")
    public ResponseEntity<?> updateBooking(@PathVariable Long bookingId,
                                           @RequestBody BookingRequest request) {
        Booking updated = bookingService.updateBooking(bookingId, request);
        return ResponseEntity.ok(updated);
    }
}
