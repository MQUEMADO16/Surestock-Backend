package com.surestock.controller;

import com.surestock.dto.BusinessSettingsDTO;
import com.surestock.model.Business;
import com.surestock.model.Role;
import com.surestock.model.User;
import com.surestock.service.BusinessService;
import com.surestock.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    @Autowired
    private BusinessService businessService;

    @Autowired
    private UserService userService;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    /**
     * Retrieves the current settings for the authenticated user's business.
     * Accessible by both Owners and Employees (employees need to know currency/tax).
     */
    @GetMapping("/settings")
    public Business getSettings(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return businessService.getBusinessById(user.getBusinessId());
    }

    /**
     * Updates business settings.
     * Only Owners can modify business configuration.
     */
    @PutMapping("/settings")
    public Business updateSettings(@RequestBody BusinessSettingsDTO dto,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);

        if (user.getRole() != Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Business Owners can modify settings.");
        }

        return businessService.updateSettings(user.getBusinessId(), dto);
    }
}