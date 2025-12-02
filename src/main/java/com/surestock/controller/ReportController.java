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
        User user = userService.findByEmail(userDetails.getUsername());
        return user.getBusinessId();
    }

    // Endpoint: GET /api/reports/INVENTORY or /api/reports/SALES
    @GetMapping("/{type}")
    public ReportResultDTO getReport(@PathVariable String type, @AuthenticationPrincipal UserDetails userDetails) {
        // Dynamically get the business ID from the logged-in user
        Long businessId = getCurrentBusinessId(userDetails);

        return reportService.getReport(type.toUpperCase(), businessId);
    }
}