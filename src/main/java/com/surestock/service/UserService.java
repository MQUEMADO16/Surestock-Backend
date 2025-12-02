package com.surestock.service;

import com.surestock.model.Role;
import com.surestock.model.User;
import com.surestock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Registers a new user with a hashed password.
     */
    public User registerUser(String email, String rawPassword, Role role, Long businessId) {
        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email is already in use: " + email);
        }

        // Hash the password
        String hashedPassword = encoder.encode(rawPassword);

        // Create and Save the User
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(hashedPassword);
        newUser.setRole(role);
        newUser.setBusinessId(businessId);

        return userRepository.save(newUser);
    }

    /**
     * Authenticates a user by email and password.
     * Returns the User object if successful, throws exception if failed.
     */
    public User authenticate(String email, String rawPassword) {
        // Find the user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Check password match
        if (!encoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }
}