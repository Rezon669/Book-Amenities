package com.app.bookamenities.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingRequest {

    private Long userId;
    private String amenityName;
    private String date;
    private String startTime;
    private String endTime;
}


