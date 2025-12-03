package com.surestock.controller;

import com.surestock.dto.transaction.SaleRequestDTO;
import com.surestock.model.SalesTransaction;
import com.surestock.model.User;
import com.surestock.service.SalesService;
import com.surestock.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private SalesService salesService;

    @Autowired
    private UserService userService;

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    /**
     * Records a new sale.
     * Triggered when the user hits "Checkout" on the frontend.
     */
    @PostMapping
    public List<SalesTransaction> createTransaction(@RequestBody SaleRequestDTO request,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);

        // Both Owners and Employees can make sales, so no Role check needed.
        return salesService.processSale(user.getBusinessId(), request);
    }
}