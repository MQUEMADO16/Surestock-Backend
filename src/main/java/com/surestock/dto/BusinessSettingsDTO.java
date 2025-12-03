package com.surestock.dto;

import lombok.Data;

@Data
public class BusinessSettingsDTO {
    private String name;
    private String currency;
    private Double taxRate;
    private Integer lowStockThreshold;
    private String contactAddress;
}