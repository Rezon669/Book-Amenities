package com.app.bookamenities.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class BookingRequest {

    private Long userId;
    private String amenityName;
    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate bookingDate;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
}


