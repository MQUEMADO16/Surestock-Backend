package com.surestock.service.report;

import com.surestock.dto.report.ReportResultDTO;
import com.surestock.model.Product;
import com.surestock.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DeadStockReportStrategy implements IReportStrategy {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public ReportResultDTO generate(Long businessId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Product> deadProducts = productRepository.findDeadStock(businessId, thirtyDaysAgo);

        List<Map<String, Object>> tableData = new ArrayList<>();
        double totalWastedCapital = 0.0;

        for (Product p : deadProducts) {
            if (p.getQuantity() > 0) {
                double tiedUp = (p.getCost() != null ? p.getCost() : 0.0) * p.getQuantity();
                totalWastedCapital += tiedUp;

                Map<String, Object> row = new HashMap<>();
                row.put("name", p.getName());
                row.put("sku", p.getSku());
                row.put("quantity", p.getQuantity());
                row.put("value", tiedUp); // Capital tied up
                tableData.add(row);
            }
        }

        String summary = String.format("Total Capital Tied in Dead Stock: $%.2f", totalWastedCapital);

        return new ReportResultDTO(
                "Dead Stock Report (No sales in 30 days)",
                summary,
                "TABLE",
                tableData
        );
    }

    @Override
    public String getType() {
        return "DEAD_STOCK";
    }
}