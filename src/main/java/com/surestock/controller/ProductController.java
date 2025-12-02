package com.surestock.controller;

import com.surestock.dto.ProductDTO;
import com.surestock.model.Product;
import com.surestock.model.Role;
import com.surestock.model.User;
import com.surestock.service.ProductService;
import com.surestock.service.UserService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    // DTO for stock-only updates
    @Data
    public static class StockUpdateRequest {
        private int quantityChange;
    }

    // DTO for full product editing
    @Data
    public static class ProductDetailsUpdateRequest {
        private String name;
        private String sku;
        private Double price;
        private Double cost;
        private Integer reorderThreshold;
        // Note: Quantity is intentionally excluded to force use of the specific stock endpoint,
        // preventing accidental overrides during concurrent edits.
    }

    private User getCurrentUser(UserDetails userDetails) {
        return userService.findByEmail(userDetails.getUsername());
    }

    @GetMapping
    public List<Product> getProducts(@AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return productService.getAllProducts(user.getBusinessId());
    }

    @PostMapping
    public Product createProduct(@RequestBody ProductDTO dto, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        return productService.createProduct(dto, user.getBusinessId());
    }

    /**
     * Endpoint 1: Quick Stock Adjustment
     * Usage: Scanners, +/- buttons on dashboard.
     */
    @PatchMapping("/{id}/stock")
    public Product updateStock(@PathVariable Long id,
                               @RequestBody StockUpdateRequest request,
                               @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);

        Product targetProduct = productService.getProductById(id);
        if (!targetProduct.getBusinessId().equals(user.getBusinessId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }

        return productService.updateStock(id, request.getQuantityChange());
    }

    /**
     * Endpoint 2: Edit Product Details
     * Usage: "Edit Product" form.
     */
    @PatchMapping("/{id}")
    public Product updateProductDetails(@PathVariable Long id,
                                        @RequestBody ProductDetailsUpdateRequest request,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);

        // Verify product belongs to this business
        Product targetProduct = productService.getProductById(id);
        if (!targetProduct.getBusinessId().equals(user.getBusinessId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }

        return productService.updateProductDetails(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);

        // Role Check
        if (user.getRole() != Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Business Owners can delete products.");
        }

        // Ownership Check (Double check before delete)
        Product targetProduct = productService.getProductById(id);
        if (!targetProduct.getBusinessId().equals(user.getBusinessId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied.");
        }

        productService.deleteProduct(id, user.getBusinessId());
        return ResponseEntity.noContent().build();
    }
}