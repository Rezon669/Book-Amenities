package com.app.bookamenities.controller;

import com.app.bookamenities.dto.Response;
import com.app.bookamenities.dto.UserRequest;
import com.app.bookamenities.dto.UserResponse;
import com.app.bookamenities.entity.User;
import com.app.bookamenities.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/book-amenities/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<?> addUser(@Valid @RequestBody UserRequest userRequest){
        userService.addUser(userRequest);
        Response response = new Response();
        response.setMessage("User created Successfully");
        response.setStatuscode(HttpStatus.CREATED.value());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUser(@PathVariable("userId") Long userId){
        User user = userService.getUserDetails(userId);
        UserResponse userResponse = new UserResponse();
        BeanUtils.copyProperties(user, userResponse);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") Long userId){

        boolean deleted = userService.deleteUserDetails(userId);
        Response response = new Response();

        if (!deleted) {
            System.out.println("insde the flase condition");
            response.setStatuscode(HttpStatus.NOT_FOUND.value());
            response.setMessage("User not found with ID: " + userId);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        response.setStatuscode(HttpStatus.OK.value());
        response.setMessage("User record deleted successfully " + userId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
