package com.surestock.service;

import com.surestock.model.Business;
import com.surestock.model.Role;
import com.surestock.model.User;
import com.surestock.repository.BusinessRepository;
import com.surestock.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessRepository businessRepository;

    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    @Transactional
    public User registerOwner(String email, String rawPassword, String businessName) {
        if (userRepository.findByEmail(email).isPresent()) {
            // Returns HTTP 409 Conflict instead of 500 Error
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already in use");
        }

        // Create Business
        Business newBusiness = new Business();
        newBusiness.setName(businessName);
        newBusiness = businessRepository.save(newBusiness);

        // Create User
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(encoder.encode(rawPassword));
        newUser.setRole(Role.OWNER);
        newUser.setBusinessId(newBusiness.getId());

        return userRepository.save(newUser);
    }

    public User authenticate(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!encoder.matches(rawPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return user;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }
}