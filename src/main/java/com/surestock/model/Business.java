package com.surestock.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "businesses")
@Data
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Business {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Configurable Fields

    @Column(nullable = false)
    private String currency = "USD"; // Default currency

    @Column(name = "tax_rate", nullable = false)
    private Double taxRate = 0.0; // Default tax rate (percentage, e.g., 0.08 for 8%)

    @Column(name = "low_stock_threshold", nullable = false)
    private Integer lowStockThreshold = 10; // Default threshold for low stock alerts

    @Column(name = "contact_address")
    private String contactAddress; // Optional contact address
}