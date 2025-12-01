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

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantitySold;
    private Double salePrice;

    private LocalDateTime timestamp;

    @Column(name = "business_id", nullable = false)
    private Long businessId;
}