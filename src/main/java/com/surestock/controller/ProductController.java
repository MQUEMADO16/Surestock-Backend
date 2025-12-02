package com.surestock.controller;

import com.surestock.dto.ProductDTO;
import com.surestock.model.Product;
import com.surestock.model.Role;
import com.surestock.model.User;
import com.surestock.service.ProductService;
import com.surestock.service.UserService;
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

    @PatchMapping("/{id}/stock")
    public Product updateStock(@PathVariable Long id, @RequestParam int quantityChange) {
        return productService.updateStock(id, quantityChange);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);

        // Check Role: Only OWNER can delete
        if (user.getRole() != Role.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only Business Owners can delete products.");
        }

        productService.deleteProduct(id, user.getBusinessId());
        return ResponseEntity.noContent().build();
    }
}