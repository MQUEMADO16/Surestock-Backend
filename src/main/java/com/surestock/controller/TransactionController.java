package com.surestock.controller;

import com.surestock.dto.transaction.SaleRequestDTO;
import com.surestock.dto.transaction.TransactionResponseDTO;
import com.surestock.model.SalesTransaction;
import com.surestock.model.User;
import com.surestock.service.SalesService;
import com.surestock.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @GetMapping()
    public ResponseEntity<List<TransactionResponseDTO>> getTransactionHistory(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<SalesTransaction> transactions = salesService.getHistory(user.getBusinessId());

        List<TransactionResponseDTO> dtos = transactions.stream()
                .map(tx -> TransactionResponseDTO.builder()
                        .id(tx.getId())
                        .timestamp(tx.getTimestamp())
                        .quantity(tx.getQuantitySold())
                        .totalPrice(tx.getTotalPrice())
                        .productName(tx.getProduct().getName())
                        .productSku(tx.getProduct().getSku())
                        .build()
                )
                .toList();

        return ResponseEntity.ok(dtos);
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