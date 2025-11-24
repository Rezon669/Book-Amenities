package com.app.bookamenities.repository;

import com.app.bookamenities.entity.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {

    //@Query("SELECT u.userId from User u WHERE u.username: username ")
    User findByUsername(String username);
}
