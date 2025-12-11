package com.surestock.service.report;

import com.surestock.dto.report.ReportResultDTO;
import com.surestock.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InventoryReportStrategy implements IReportStrategy {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ReportResultDTO generate(Long businessId) {
        var products = productRepository.findByBusinessId(businessId);

        // Logic: Sum (Price * Quantity) for all products
        double totalValue = products.stream()
                .mapToDouble(p -> {
                    double price = (p.getPrice() != null) ? p.getPrice() : 0.0;
                    int qty = (p.getQuantity() != null) ? p.getQuantity() : 0;
                    return price * qty;
                })
                .sum();

        return new ReportResultDTO(
                "Inventory Valuation Report",
                "Total Stock Value: $" + String.format("%.2f", totalValue)
        );
    }

    @Override
    public String getType() {
        return "INVENTORY";
    }
}