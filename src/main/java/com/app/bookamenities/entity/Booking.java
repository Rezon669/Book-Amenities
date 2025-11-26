package com.app.bookamenities.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    private Long userId;

    @NotBlank
    private String amenityName;

    @NotBlank
    private String bookingDate;

    @NotBlank
    private String fromTime;

    @NotBlank
    private String toTime;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdDate;
}
