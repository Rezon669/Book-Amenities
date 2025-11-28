package com.app.bookamenities.service;

import com.app.bookamenities.dto.BookingRequest;
import com.app.bookamenities.entity.Booking;
import com.app.bookamenities.entity.User;
import com.app.bookamenities.exception.CustomException;
import com.app.bookamenities.repository.BookingRepository;
import com.app.bookamenities.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;


import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final SnsClient snsClient;

    BookingService(BookingRepository bookingRepository, UserRepository userRepository, SnsClient snsClient){
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.snsClient = snsClient;
    }
    public Booking bookSlot(BookingRequest request) {

        validateBookingDetails(request);

        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setAmenityName(request.getAmenityName());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setBookingDate(request.getBookingDate());
        booking.setCreatedDate(LocalDateTime.now());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException("User not found"));

        Booking savedBooking = bookingRepository.save(booking);

        String message =
                "Hi " + user.getFirstName()+",\n" +
                        "Booking Confirmed!\n" +
                        "Booking ID: " + savedBooking.getBookingId() + "\n" +
                        "Amenity: " + request.getAmenityName() + "\n" +
                        "Date: " + request.getBookingDate() + "\n" +
                        "From: " + request.getStartTime() + "\n" +
                        "To: " + request.getEndTime();

     //   sendSms(user.getMobile(), message);

        return savedBooking;
    }

    public void validateBookingDetails(BookingRequest request){

        List<Booking> userBookings = bookingRepository.findByUserIdAndBookingDate(request.getUserId(), request.getBookingDate());

        for (Booking b : userBookings) {

            if (b.getAmenityName().equalsIgnoreCase(request.getAmenityName())) {
                throw new CustomException("You can book this amenity only once per day");
            }

            boolean isOverlapping =
                    (request.getStartTime().isBefore(b.getEndTime()) && request.getEndTime().isAfter(b.getStartTime()));

            if (isOverlapping) {
                throw new CustomException(
                        "You already have a booking that overlaps with this time slot"
                );
            }
        }

        if (request.getBookingDate().isBefore(LocalDate.now())) {
            throw new CustomException("Booking date cannot be in the past");
        }

        if (request.getBookingDate().isEqual(LocalDate.now())) {
            if (request.getStartTime().isBefore(LocalTime.now())) {
                throw new CustomException("Start time cannot be before current time");
            }
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new CustomException("End time cannot be earlier than Start time");
        }

        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
        if (request.getAmenityName().equalsIgnoreCase("Convention Hall")) {

            if (duration.toHours() > 12) {
                throw new CustomException("Convention Hall can be booked for up to 12 hours only");
            }

        } else {
            if (duration.toMinutes() > 120) {
                throw new CustomException("This amenity cannot be booked for more than 2 hours");
            }
        }

        if (duration.isZero() || duration.isNegative()) {
            throw new CustomException("Invalid time selection");
        }

        List<Booking> allBookingsForDate =
                bookingRepository.findByBookingDateAndAmenityName(request.getBookingDate(), request.getAmenityName());

        for (Booking b : allBookingsForDate) {

            LocalTime existingFrom = b.getStartTime();
            LocalTime existingTo = b.getEndTime();

            boolean overlaps = (request.getStartTime().isBefore(existingTo) && request.getEndTime().isAfter(existingFrom));

            if (overlaps) {
                throw new CustomException(
                        "This time slot is already taken by another user"
                );
            }
        }

        log.info("Booking details validation completed");
    }

    public void sendSms(String phoneNumber, String message) {

        PublishRequest request = PublishRequest.builder()
                .phoneNumber( "+91" + phoneNumber)
                .message(message)
                .build();

        PublishResponse response = snsClient.publish(request);

        System.out.println("Message ID: " + response.messageId());
    }

    public List<Booking> getPastBookings(Long userId) {
        LocalDate today = LocalDate.now();
        return bookingRepository.findPastBookings(userId, today);
    }

    public List<Booking> getUpcomingBookings(Long userId) {
        LocalDate today = LocalDate.now();
        return bookingRepository.findUpcomingBookings(userId, today);
    }

    public void deleteBooking(Long bookingId, Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CustomException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new CustomException("You are not allowed to delete this booking");
        }

        if (booking.getBookingDate().isBefore(LocalDate.now())) {
            throw new CustomException("Past bookings cannot be deleted");
        }

        if (booking.getBookingDate().isEqual(LocalDate.now())) {
            LocalTime startTime = booking.getStartTime();

            if (startTime.isBefore(LocalTime.now())) {
                throw new CustomException("You cannot delete a booking that already started");
            }
        }

        bookingRepository.delete(booking);

    }

    public Booking updateBooking(Long bookingId, BookingRequest request) {

        log.info("inside the update booking {}", request);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CustomException("Booking not found"));

        validateBookingDetails(request);

        // Apply updates
        booking.setAmenityName(request.getAmenityName());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setBookingDate(request.getBookingDate());

        return bookingRepository.save(booking);
    }



}
