package com.surestock.controller;

import com.surestock.model.User;
import com.surestock.service.ReportService;
import com.surestock.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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

    /**
     * Endpoint to fetch any report type, accepting optional query parameters for filtering (e.g., date range).
     * GET /api/reports/{TYPE}?startDate=2025-01-01
     * * @param type The type of report to generate (e.g., INVENTORY, PROFIT).
     * @param userDetails The security principal to identify the business.
     * @param params All optional query parameters (e.g., startDate, endDate).
     * @return A structured Map containing the report data.
     */
    @GetMapping("/{type}")
    public Map<String, Object> getReport(
            @PathVariable String type,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Map<String, String> params) { // Accepts all query parameters

        // Dynamically get the business ID from the logged-in user
        Long businessId = getCurrentBusinessId(userDetails);

        // Pass the parameters map to the service
        return reportService.getReport(type.toUpperCase(), businessId, params);
    }
}