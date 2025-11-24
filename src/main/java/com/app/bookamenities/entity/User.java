package com.app.bookamenities.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@Entity
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int userId;

    @NotBlank(message = "Username is required field")
    private String username;

    @NotBlank(message = "First name is required field")
    private String firstName;

    @NotBlank(message = "Last name is required field")
    private String lastName;

    @NotBlank(message = "Password is required field")
    private String password;

    @NotBlank(message = "Mobile number is required field")
    @Pattern(regexp = "^[6-9]\\d{9}$", message="Invalid Mobile number")
    private String mobile;

}
