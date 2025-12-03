package com.surestock.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales_transactions")
@Data
public class SalesTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which business made this sale
    @Column(name = "business_id", nullable = false)
    private Long businessId;

    // Which product was sold
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity_sold", nullable = false)
    private Integer quantitySold;

    // Snapshot of the total price at the moment of sale
    // (We store this because product prices might change later)
    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}