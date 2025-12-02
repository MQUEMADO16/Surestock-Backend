package com.surestock.controller;

import com.surestock.dto.auth.LoginRequestDTO;
import com.surestock.dto.auth.RegisterRequestDTO;
import com.surestock.dto.auth.UserResponseDTO;
import com.surestock.model.User;
import com.surestock.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public UserResponseDTO register(@RequestBody RegisterRequestDTO request) {
        // Pass the business NAME, not the ID.
        // The service will handle creating the ID.
        User newUser = userService.registerOwner(
                request.getEmail(),
                request.getPassword(),
                request.getBusinessName()
        );
        return new UserResponseDTO(newUser);
    }

    @PostMapping("/login")
    public UserResponseDTO login(@RequestBody LoginRequestDTO request, HttpServletRequest servletRequest) {
        // Authenticate (Checks DB + Password automatically)
        // If this fails, it throws BadCredentialsException (Caught by GlobalHandler)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // Set Session
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Force the session to persist
        HttpSession session = servletRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        // Fetch User Details for the Response
        User user = userService.findByEmail(request.getEmail());

        return new UserResponseDTO(user);
    }
}