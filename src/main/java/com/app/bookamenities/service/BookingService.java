package com.app.bookamenities.service;

import com.app.bookamenities.dto.BookingRequest;
import com.app.bookamenities.entity.Booking;
import com.app.bookamenities.entity.User;
import com.app.bookamenities.exception.CustomException;
import com.app.bookamenities.repository.BookingRepository;
import com.app.bookamenities.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.util.List;

@Service
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    BookingService(BookingRepository bookingRepository, UserRepository userRepository){
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }
    public Booking bookSlot(BookingRequest request) {

        log.info("Validating the booking information");
        validateBookingDetails(request);

        Booking booking = new Booking();
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("No user record found with this Id"));
        booking.setUser(user);
        booking.setAmenityName(request.getAmenityName());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setSlot(request.getSlot());
        booking.setRoomNumber(request.getRoomNumber());
        booking.setBookingDate(request.getBookingDate());
        booking.setCreatedDate(LocalDateTime.now());

        log.info("Creating a booking slot");

        Booking savedBooking = bookingRepository.save(booking);

//        String message =
//                "Hi " + user.getFirstName()+",\n" +
//                        "Booking Confirmed!\n" +
//                        "Booking ID: " + savedBooking.getBookingId() + "\n" +
//                        "Amenity: " + request.getAmenityName() + "\n" +
//                        "Date: " + request.getBookingDate() + "\n" +
//                        "From: " + request.getStartTime() + "\n" +
//                        "To: " + request.getEndTime();

        return savedBooking;
    }

    public void validateBookingDetails(BookingRequest request){

        List<Booking> userBookings = bookingRepository.findByUser_UserIdAndBookingDate(request.getUserId(), request.getBookingDate());

        if(request.getAmenityName().equalsIgnoreCase("Guest Rooms")) {
            if (request.getSlot().equalsIgnoreCase("Morning")) {
                request.setStartTime(LocalTime.parse("06:00"));
                request.setEndTime(LocalTime.parse("18:00"));
            } else if (request.getSlot().equalsIgnoreCase("Night")) {
                request.setStartTime(LocalTime.parse("18:00"));
                request.setEndTime(LocalTime.parse("23:59"));
            } else if (request.getSlot().equalsIgnoreCase("Full day")) {
                request.setStartTime(LocalTime.parse("00:00"));
                request.setEndTime(LocalTime.parse("23:59"));
            }
        }else if(request.getAmenityName().equalsIgnoreCase("Convention Hall")){
            if (request.getSlot().equalsIgnoreCase("Slot 1")) {
                request.setStartTime(LocalTime.parse("03:00"));
                request.setEndTime(LocalTime.parse("12:00"));
            }else if (request.getSlot().equalsIgnoreCase("Slot 2")) {
                request.setStartTime(LocalTime.parse("15:00"));
                request.setEndTime(LocalTime.parse("23:59"));
            }
        }

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
        if (request.getAmenityName().equalsIgnoreCase("Swimming Pool") ||
                request.getAmenityName().equalsIgnoreCase("Badminton Court") ||
                request.getAmenityName().equalsIgnoreCase("BasketBall Court") ||
                request.getAmenityName().equalsIgnoreCase("Table Tennis")) {
            if (duration.toMinutes() > 120) {
                throw new CustomException("This amenity cannot be booked for more than 2 hours");
            }
        }

        if(request.getAmenityName().equalsIgnoreCase("Convention Hall")){
            int currentYear = request.getBookingDate().getYear();

            int count = bookingRepository.countBookingsForYear(request.getUserId(),
                    request.getAmenityName(), currentYear);

            if (count >= 2) {
                throw new CustomException("Convention Hall can be booked only 2 times in a year");
            }
        }
//        if (request.getAmenityName().equalsIgnoreCase("Convention Hall")) {
//
//            if (duration.toHours() > 12) {
//                throw new CustomException("Convention Hall can be booked for up to 12 hours only");
//            }
//
//        } else {
//            if (duration.toMinutes() > 120) {
//                throw new CustomException("This amenity cannot be booked for more than 2 hours");
//            }
//        }

        List<String> weeklyRestricted = List.of("Swimming Pool", "Table Tennis", "Badminton Court", "BasketBall Court", "Table Tennis");

        if (weeklyRestricted.stream().anyMatch(a -> a.equalsIgnoreCase(request.getAmenityName()))) {

            int currentWeek = request.getBookingDate().get(ChronoField.ALIGNED_WEEK_OF_YEAR);
            log.info("current week " + currentWeek);
            int currentYear = request.getBookingDate().getYear();
            log.info("current year " + currentYear);

            int weeklyCount = bookingRepository.countWeeklyBookings(
                    request.getUserId(),
                    request.getAmenityName(),
                    currentWeek,
                    currentYear
            );

            log.info("Weekly count "+ weeklyCount);

            if (weeklyCount >= 1) {
                throw new CustomException(request.getAmenityName() + " can be booked only 3 times per week per user");
            }
        }

        if (request.getAmenityName().equalsIgnoreCase("Guest Rooms")) {

            int currentMonth = request.getBookingDate().getMonthValue();
            int currentYear = request.getBookingDate().getYear();

            int monthlyCount = bookingRepository.countMonthlyBookings(
                    request.getUserId(),
                    request.getAmenityName(),
                    currentMonth,
                    currentYear
            );

            if (monthlyCount >= 3) {
                throw new CustomException("Guest Rooms can be booked only 3 times per month per user");
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

//        PublishRequest request = PublishRequest.builder()
//                .phoneNumber( "+91" + phoneNumber)
//                .message(message)
//                .build();

       // PublishResponse response = snsClient.publish(request);

      //  System.out.println("Message ID: " + response.messageId());
    }

    public List<Booking> getPastBookings(Long userId) {
        LocalDate today = LocalDate.now();
        return bookingRepository.findPastBookings(userId, today);
    }

    public List<Booking> getUpcomingBookings(Long userId) {
        LocalDate today = LocalDate.now();
        log.info("Fetching upcoming bookings");
        return bookingRepository.findUpcomingBookings(userId, today);
    }

    public void deleteBooking(Long bookingId, Long userId) {

        log.info("Verifying booking id : {}", bookingId);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CustomException("Booking not found"));

        if (!booking.getUser().getUserId().equals(userId)) {
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
        log.info("Deleting booking id {}", bookingId);

        bookingRepository.delete(booking);
    }

    public Booking updateBooking(Long bookingId, BookingRequest request) {

        log.info("Validating booking details");

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new CustomException("Booking not found"));

        validateBookingDetails(request);

        booking.setAmenityName(request.getAmenityName());
        booking.setStartTime(request.getStartTime());
        booking.setEndTime(request.getEndTime());
        booking.setBookingDate(request.getBookingDate());

        log.info("updating booking details");

        return bookingRepository.save(booking);
    }

}
