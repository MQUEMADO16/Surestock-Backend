package com.surestock.service.report;

import com.surestock.dto.report.ReportResultDTO;
import com.surestock.model.SalesTransaction;
import com.surestock.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class SalesReportStrategy implements IReportStrategy {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public ReportResultDTO generate(Long businessId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysAgo = now.minusDays(30);

        List<SalesTransaction> recentTransactions = transactionRepository
                .findByBusinessIdAndTimestampBetween(businessId, thirtyDaysAgo, now);

        // Group by Date (YYYY-MM-DD) and Sum Revenue
        Map<String, Double> dailyRevenue = new TreeMap<>(); // TreeMap keeps dates sorted
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (SalesTransaction t : recentTransactions) {
            String dateKey = t.getTimestamp().format(formatter);
            double amount = t.getTotalPrice() != null ? t.getTotalPrice() : 0.0;
            dailyRevenue.put(dateKey, dailyRevenue.getOrDefault(dateKey, 0.0) + amount);
        }

        // Convert Map to List of Objects for Frontend
        List<Map<String, Object>> chartData = new ArrayList<>();
        double totalRevenue = 0;

        for (Map.Entry<String, Double> entry : dailyRevenue.entrySet()) {
            Map<String, Object> point = new HashMap<>();
            point.put("date", entry.getKey());   // X-Axis
            point.put("revenue", entry.getValue()); // Y-Axis
            chartData.add(point);
            totalRevenue += entry.getValue();
        }

        return new ReportResultDTO(
                "Revenue Trends (30 Days)",
                "Total Revenue: $" + String.format("%.2f", totalRevenue),
                "LINE", // Tell frontend to use a Line Chart
                chartData
        );
    }

    @Override
    public String getType() {
        return "SALES";
    }
}