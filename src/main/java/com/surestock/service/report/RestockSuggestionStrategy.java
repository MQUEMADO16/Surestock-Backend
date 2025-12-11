package com.surestock.service.report;

import com.surestock.dto.report.ReportResultDTO;
import com.surestock.model.Product;
import com.surestock.model.SalesTransaction;
import com.surestock.repository.ProductRepository;
import com.surestock.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class RestockSuggestionStrategy implements IReportStrategy {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public ReportResultDTO generate(Long businessId) {
        List<Product> products = productRepository.findByBusinessId(businessId);

        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<SalesTransaction> recentSales = transactionRepository
                .findByBusinessIdAndTimestampBetween(businessId, thirtyDaysAgo, LocalDateTime.now());

        // Map Product ID -> Total Qty Sold
        Map<Long, Integer> salesMap = recentSales.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getProduct().getId(),
                        Collectors.summingInt(SalesTransaction::getQuantitySold)
                ));

        List<Map<String, Object>> tableData = new ArrayList<>();
        int criticalItems = 0;

        for (Product p : products) {
            int soldLast30 = salesMap.getOrDefault(p.getId(), 0);
            if (soldLast30 > 0) {
                double dailyVelocity = soldLast30 / 30.0;
                int daysUntilEmpty = (int) (p.getQuantity() / dailyVelocity);

                if (daysUntilEmpty < 14) { // Suggest restock if < 2 weeks left
                    criticalItems++;
                    Map<String, Object> row = new HashMap<>();
                    row.put("name", p.getName());
                    row.put("current_stock", p.getQuantity());
                    row.put("daily_sales", String.format("%.1f", dailyVelocity));
                    row.put("days_remaining", daysUntilEmpty);
                    tableData.add(row);
                }
            }
        }

        String summary = criticalItems > 0
                ? "Prediction: " + criticalItems + " items will run out within 14 days."
                : "Inventory velocity is stable.";

        return new ReportResultDTO(
                "Restock Suggestions (Velocity Analysis)",
                summary,
                "TABLE",
                tableData
        );
    }

    @Override
    public String getType() {
        return "RESTOCK";
    }
}