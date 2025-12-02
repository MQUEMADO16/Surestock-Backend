package com.surestock.dto;

import lombok.Data;

@Data
public class ProductDTO {
    // Basic Details
    private String name;
    private String sku;
    private Double price; // Retail Price (What customers pay)
    private Double cost;  // Wholesale Cost (What the owner pays)

    // Inventory Management
    private Integer quantity;
    private Integer reorderThreshold;
}