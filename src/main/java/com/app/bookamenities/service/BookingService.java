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
import java.time.format.DateTimeFormatter;
import java.util.Date;
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

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate bookingDate = LocalDate.parse(request.getDate(), dateFormatter);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startTime = LocalTime.parse(request.getStartTime(), formatter);
        LocalTime endTime = LocalTime.parse(request.getEndTime(), formatter);

        LocalDate today = LocalDate.now();

        List<Booking> userBookings = bookingRepository.findByUserIdAndBookingDate(request.getUserId(), request.getDate());

        for (Booking b : userBookings) {

            if (b.getAmenityName().equalsIgnoreCase(request.getAmenityName())) {
                throw new CustomException("You can book this amenity only once per day");
            }

            LocalTime existingFrom = LocalTime.parse(b.getFromTime(), formatter);
            LocalTime existingTo = LocalTime.parse(b.getToTime(), formatter);

            boolean isOverlapping =
                    (startTime.isBefore(existingTo) && endTime.isAfter(existingFrom));

            if (isOverlapping) {
                throw new CustomException(
                        "You already have a booking that overlaps with this time slot"
                );
            }
        }

        if (bookingDate.isBefore(today)) {
            throw new CustomException("Booking date cannot be in the past");
        }

        if (bookingDate.isEqual(today)) {
            if (startTime.isBefore(LocalTime.now())) {
                throw new CustomException("Start time cannot be before current time");
            }
        }

        if (endTime.isBefore(startTime)) {
            throw new CustomException("End time cannot be earlier than Start time");
        }

        Duration duration = Duration.between(startTime, endTime);
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
                bookingRepository.findByBookingDateAndAmenityName(request.getDate(), request.getAmenityName());

        for (Booking b : allBookingsForDate) {

            LocalTime existingFrom = LocalTime.parse(b.getFromTime(), formatter);
            LocalTime existingTo = LocalTime.parse(b.getToTime(), formatter);

            boolean overlaps = (startTime.isBefore(existingTo) && endTime.isAfter(existingFrom));

            if (overlaps) {
                throw new CustomException(
                        "This time slot is already taken by another user"
                );
            }
        }

        log.info("Booking details validation completed");

        Booking booking = new Booking();
        booking.setUserId(request.getUserId());
        booking.setAmenityName(request.getAmenityName());
        booking.setFromTime(request.getStartTime());
        booking.setToTime(request.getEndTime());
        booking.setBookingDate(request.getDate());
        booking.setCreatedDate(LocalDateTime.now());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new CustomException("User not found"));

        Booking savedBooking = bookingRepository.save(booking);

        String mobile = user.getMobile();

        String message =
                "Hi " + user.getFirstName()+",\n" +
                        "Booking Confirmed!\n" +
                        "Booking ID: " + savedBooking.getBookingId() + "\n" +
                        "Amenity: " + request.getAmenityName() + "\n" +
                        "Date: " + request.getDate() + "\n" +
                        "From: " + request.getStartTime() + "\n" +
                        "To: " + request.getEndTime();

     //   sendSms(mobile, message);

        return savedBooking;

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
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        return bookingRepository.findPastBookings(userId, today);
    }

    public List<Booking> getUpcomingBookings(Long userId) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        return bookingRepository.findUpcomingBookings(userId, today);
    }

    public String deleteBooking(Long bookingId, Long userId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CustomException("Booking not found"));

        if (!booking.getUserId().equals(userId)) {
            throw new CustomException("You are not allowed to delete this booking");
        }

        LocalDate bookingDate = LocalDate.parse(booking.getBookingDate(),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"));

        LocalDate today = LocalDate.now();

        if (bookingDate.isBefore(today)) {
            throw new CustomException("Past bookings cannot be deleted");
        }

        if (bookingDate.isEqual(today)) {
            LocalTime startTime = LocalTime.parse(booking.getFromTime(),
                    DateTimeFormatter.ofPattern("HH:mm"));

            if (startTime.isBefore(LocalTime.now())) {
                throw new CustomException("You cannot delete a booking that already started");
            }
        }

        bookingRepository.delete(booking);

        return "Booking deleted successfully";
    }


}
