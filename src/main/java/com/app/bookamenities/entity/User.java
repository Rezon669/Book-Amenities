package com.app.bookamenities.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Data
@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long userId;

    @NotBlank(message = "Username is required field")
    private String username;

    @NotBlank(message = "First name is required field")
    private String firstName;

    @NotBlank(message = "Last name is required field")
    private String lastName;

    @NotBlank(message = "Flat number is a required field")
    private String flatNumber;

    @NotBlank
    private String block;

    @NotBlank(message = "Password is required field")
    private String password;

    @NotBlank(message = "Mobile number is required field")
    @Pattern(regexp = "^[6-9]\\d{9}$", message="Invalid Mobile number")
    private String mobile;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private Date createdDate;

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonIgnore
    private List<Booking> bookings = new ArrayList<>();

}
