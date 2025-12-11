package com.surestock.service.report;

import com.surestock.model.Product;
import com.surestock.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Generates a report showing all products where the current quantity is at or below
 * the reorder threshold defined by the business owner.
 */
@Component
public class LowStockReportStrategy implements IReportStrategy {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Map<String, Object> generate(Long businessId, Map<String, String> parameters) {
        List<Product> lowStockProducts = productRepository.findLowStockProducts(businessId);

        String summary = String.format("Found %d items critically low on stock.", lowStockProducts.size());

        // Return structured list of low items
        List<Map<String, Object>> details = lowStockProducts.stream()
                .map(p -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("productId", p.getId());
                    item.put("name", p.getName());
                    item.put("currentQuantity", p.getQuantity());
                    item.put("threshold", p.getReorderThreshold());
                    item.put("status", p.getQuantity() == 0 ? "Out of Stock" : "Low Stock");
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> report = new HashMap<>();
        report.put("title", "Low Stock Alert Report");
        report.put("summary", summary);
        report.put("data", details);
        report.put("type", getType());
        return report;
    }

    @Override
    public String getType() {
        return "LOW_STOCK";
    }
}