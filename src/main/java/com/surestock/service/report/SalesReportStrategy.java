package com.surestock.service.report;

import com.surestock.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Calculates the total sales revenue over a period, supporting date range filtering via parameters.
 */
@Component
public class SalesReportStrategy implements IReportStrategy {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public Map<String, Object> generate(Long businessId, Map<String, String> parameters) {
        LocalDateTime startDate = parameters.containsKey("startDate")
                ? LocalDateTime.parse(parameters.get("startDate"))
                : LocalDateTime.MIN;

        LocalDateTime endDate = parameters.containsKey("endDate")
                ? LocalDateTime.parse(parameters.get("endDate"))
                : LocalDateTime.MAX;

        var transactions = transactionRepository.findByBusinessIdAndTimestampBetween(businessId, startDate, endDate);

        // Logic: Sum (Sale Price * Quantity Sold)
        double totalRevenue = transactions.stream()
                .mapToDouble(t -> {
                    double price = (t.getTotalPrice() != null) ? t.getTotalPrice() : 0.0;
                    int qty = (t.getQuantitySold() != null) ? t.getQuantitySold() : 0;
                    return price * qty;
                })
                .sum();

        Map<String, Object> report = new HashMap<>();
        report.put("title", "Sales Revenue Report");
        report.put("summary", String.format("Total Revenue from %s to %s: $%.2f",
                startDate.toLocalDate(), endDate.toLocalDate(), totalRevenue));
        report.put("totalRevenue", totalRevenue);
        report.put("transactionCount", transactions.size());
        report.put("type", getType());
        return report;
    }

    @Override
    public String getType() {
        return "SALES";
    }
}