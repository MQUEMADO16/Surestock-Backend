package com.surestock.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String sku;
    private Double price;

    // Inventory Management
    private Integer quantity;
    private Integer reorderThreshold;

    @Column(name = "business_id", nullable = false)
    private Long businessId;
}