package com.surestock.service.report;

import com.surestock.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Calculates the total monetary valuation of the current inventory (based on retail price).
 */
@Component
public class InventoryReportStrategy implements IReportStrategy {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Map<String, Object> generate(Long businessId, Map<String, String> parameters) {
        var products = productRepository.findByBusinessId(businessId);

        // Logic: Sum (Price * Quantity) for all products
        double totalValue = products.stream()
                .mapToDouble(p -> {
                    double price = (p.getPrice() != null) ? p.getPrice() : 0.0;
                    int qty = (p.getQuantity() != null) ? p.getQuantity() : 0;
                    return price * qty;
                })
                .sum();

        Map<String, Object> report = new HashMap<>();
        report.put("title", "Inventory Valuation Report");
        report.put("summary", "Total Stock Value (Retail): $" + String.format("%.2f", totalValue));
        report.put("totalValue", totalValue);
        report.put("type", getType());
        return report;
    }

    @Override
    public String getType() {
        return "INVENTORY";
    }
}