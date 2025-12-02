package com.surestock.service;

import com.surestock.model.Business;
import com.surestock.model.Role;
import com.surestock.model.User;
import com.surestock.repository.BusinessRepository;
import com.surestock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Transactional // Ensures Business and User are created together, or both fail
    public User registerOwner(String email, String rawPassword, String businessName) {
        // Check if email exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email is already in use: " + email);
        }

        // Create the Business first
        Business newBusiness = new Business();
        newBusiness.setName(businessName);
        newBusiness = businessRepository.save(newBusiness); // DB generates the ID here

        // Create the User linked to that Business
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(encoder.encode(rawPassword));
        newUser.setRole(Role.OWNER); // Registration endpoint is for Owners
        newUser.setBusinessId(newBusiness.getId()); // Use the generated ID

        return userRepository.save(newUser);
    }

    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        if (!encoder.matches(rawPassword, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }
}