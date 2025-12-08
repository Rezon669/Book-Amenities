package com.app.bookamenities.repository;

import com.app.bookamenities.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByFlatNumberAndBlock(String flatNumber, String block);

    Optional<User> findById(Long userId);
}
