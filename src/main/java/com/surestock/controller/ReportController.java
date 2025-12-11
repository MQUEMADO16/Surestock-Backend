package com.surestock.controller;

import com.surestock.dto.report.ReportResultDTO;
import com.surestock.model.User;
import com.surestock.service.ReportService;
import com.surestock.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private UserService userService;

    // Helper method to get the current user's business ID
    private Long getCurrentBusinessId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalStateException("User must be logged in to view reports");
        }
        User user = userService.findByEmail(userDetails.getUsername());
        if (user == null) {
            throw new IllegalStateException("User record not found");
        }
        return user.getBusinessId();
    }

    // Endpoint: GET /api/reports/SALES (or INVENTORY, LOW_STOCK, etc.)
    @GetMapping("/{type}")
    public ReportResultDTO getReport(@PathVariable String type, @AuthenticationPrincipal UserDetails userDetails) {
        Long businessId = getCurrentBusinessId(userDetails);

        // This delegates to the factory service we just created
        return reportService.getReport(type.toUpperCase(), businessId);
    }
}