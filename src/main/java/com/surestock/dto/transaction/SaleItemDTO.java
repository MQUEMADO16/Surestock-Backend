package com.surestock.dto.transaction;

import lombok.Data;

@Data
public class SaleItemDTO {
    private Long productId;
    private int quantity;
}