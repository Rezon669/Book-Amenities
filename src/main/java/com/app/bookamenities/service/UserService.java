package com.app.bookamenities.service;

import com.app.bookamenities.dto.UserRequest;
import com.app.bookamenities.entity.User;
import com.app.bookamenities.exception.CustomException;
import com.app.bookamenities.repository.BookingRepository;
import com.app.bookamenities.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    public void addUser(@Valid UserRequest userRequest) {

        log.info("Adding the new user {}", userRequest.getUsername());
        Optional<User> existingUser = userRepository.findByUsername(userRequest.getUsername());
        if (existingUser.isPresent()) {
            throw new CustomException("Username already in use, try with another one");
        }

        Optional<User> existingUser1 = userRepository.findByFlatNumberAndBlock(userRequest.getFlatNumber(), userRequest.getBlock());
        if (existingUser1.isPresent()) {
            throw new CustomException("Found another user with same Flat number and Block");
        }

        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setFlatNumber(userRequest.getFlatNumber());
        user.setBlock(userRequest.getBlock());
        user.setMobile(userRequest.getMobile());

        log.info("Encrypting the password");
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setCreatedDate(new Date());
        log.info("Adding the new user {}", userRequest.getUsername());
        userRepository.save(user);
        log.info("Successfully added the user: {}", user.getUsername());
    }

    public User getUserDetails(Long userId) {
        log.info("Fetching user details based on User ID {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found with ID: " + userId));
    }

    public boolean deleteUserDetails(Long userId) {
        log.info("Checking is user exist with Id {}", userId);

        if (!userRepository.existsById(userId)) {
            log.info("Checking is user exist with Id {}", userId);
            return false;
        }
        log.info("Deleting all the bookings based on userID {}", userId);
        log.info("Deleting user with ID {}", userId);
        userRepository.deleteById(userId);
        return true;
    }

}
