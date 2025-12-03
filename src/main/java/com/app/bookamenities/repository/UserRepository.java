package com.app.bookamenities.repository;

import com.app.bookamenities.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByFlatNumberAndBlock(String flatNumber, String block);

    Optional<User> findById(Long userId);
}
