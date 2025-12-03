package com.app.bookamenities.service;

import com.app.bookamenities.dto.LoginRequest;
import com.app.bookamenities.dto.LoginResponse;
import com.app.bookamenities.entity.BlacklistedToken;
import com.app.bookamenities.entity.User;
import com.app.bookamenities.exception.CustomException;
import com.app.bookamenities.repository.BlacklistRepository;
import com.app.bookamenities.repository.UserRepository;
import com.app.bookamenities.security.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;

    private final BlacklistRepository blacklistRepository;

    private final JwtUtil jwtUtil;

    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, BlacklistRepository blacklistRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.blacklistRepository = blacklistRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {

        log.info("Fetching username from DB");

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException("Invalid username or password"));


        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("Invalid username or password");
        }

        log.info("Validation successful, Generating the token");

        String token = jwtUtil.generateToken(user.getUsername(), user.getUserId());

        return new LoginResponse(user.getUserId(), token);
    }

    public void logout(String token) {
        BlacklistedToken blacklistedToken = new BlacklistedToken();
        blacklistedToken.setToken(token);
        blacklistedToken.setBlacklistedAt(LocalDateTime.now());
        log.info("logging out, lease wait");

        blacklistRepository.save(blacklistedToken);
    }

    public boolean isTokenBlacklisted(String token) {
        return blacklistRepository.existsByToken(token);
    }
}
