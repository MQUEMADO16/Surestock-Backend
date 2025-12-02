package com.surestock.controller;

import com.surestock.dto.auth.LoginRequestDTO;
import com.surestock.dto.auth.RegisterRequestDTO;
import com.surestock.model.User;
import com.surestock.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequestDTO request) {
        // Pass the business NAME, not the ID.
        // The service will handle creating the ID.
        return userService.registerOwner(
                request.getEmail(),
                request.getPassword(),
                request.getBusinessName()
        );
    }

    @PostMapping("/login")
    public User login(@RequestBody LoginRequestDTO request) {
        return userService.authenticate(request.getEmail(), request.getPassword());
    }
}