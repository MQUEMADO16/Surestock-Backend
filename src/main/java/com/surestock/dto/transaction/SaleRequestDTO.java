package com.surestock.dto.transaction;

import lombok.Data;
import java.util.List;

@Data
public class SaleRequestDTO {
    private List<SaleItemDTO> items;
}