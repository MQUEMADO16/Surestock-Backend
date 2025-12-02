package com.surestock.controller;

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

    // Endpoint: POST /api/auth/register
    @PostMapping("/register")
    public User register(@RequestBody RegisterRequestDTO request) {
        // Accept Role and BusinessID from the request body
        return userService.registerUser(
                request.getEmail(),
                request.getPassword(),
                request.getRole(),
                request.getBusinessId()
        );
    }

    // Endpoint: POST /api/auth/login
    @PostMapping("/login")
    public User login(@RequestParam String email, @RequestParam String password) {
        return userService.authenticate(email, password);
    }
}