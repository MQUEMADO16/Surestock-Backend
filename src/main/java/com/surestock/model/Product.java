package com.surestock.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Import this
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
    private Double price; // Retail Price (What customers pay)
    private Double cost;  // Wholesale Cost (What the owner pays)

    // Inventory Management
    private Integer quantity;
    private Integer reorderThreshold;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Business business;

    @Column(name = "business_id", nullable = false)
    private Long businessId;
}