package com.surestock.dto;

import lombok.Data;

@Data
public class ProductDTO {
    // We don't include ID here because the database generates it.
    // We don't include BusinessID because that comes from the logged-in user (security).

    private String name;
    private String sku;
    private Double price;

    // Inventory fields
    private Integer quantity;
    private Integer reorderThreshold;
}