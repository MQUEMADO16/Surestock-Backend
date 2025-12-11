package com.surestock.service.report;

import com.surestock.dto.report.ReportResultDTO;
import com.surestock.model.Product;
import com.surestock.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LowStockReportStrategy implements IReportStrategy {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ReportResultDTO generate(Long businessId) {
        List<Product> lowStockProducts = productRepository.findLowStockProducts(businessId);
        List<Map<String, Object>> tableData = new ArrayList<>();

        for (Product p : lowStockProducts) {
            Map<String, Object> row = new HashMap<>();
            row.put("name", p.getName());
            row.put("sku", p.getSku());
            row.put("quantity", p.getQuantity());
            row.put("threshold", p.getReorderThreshold());
            tableData.add(row);
        }

        String summary = lowStockProducts.isEmpty()
                ? "Inventory is healthy."
                : "Action Needed: " + lowStockProducts.size() + " items are below reorder threshold.";

        return new ReportResultDTO(
                "Low Stock Alerts",
                summary,
                "TABLE", // Frontend should render this as a Data Grid
                tableData
        );
    }

    @Override
    public String getType() {
        return "LOW_STOCK";
    }
}