package com.surestock.service.report;

import com.surestock.dto.report.ReportResultDTO;
import com.surestock.model.Product;
import com.surestock.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TopSellersReportStrategy implements IReportStrategy {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public ReportResultDTO generate(Long businessId) {
        List<Object[]> results = transactionRepository.findTopSellingProducts(
                businessId,
                PageRequest.of(0, 5)
        );

        List<Map<String, Object>> chartData = new ArrayList<>();

        for (Object[] row : results) {
            Product p = (Product) row[0];
            Long qty = (Long) row[1];

            // Build a clean JSON object for the frontend
            Map<String, Object> point = new HashMap<>();
            point.put("name", p.getName()); // Label
            point.put("value", qty);        // Data
            chartData.add(point);
        }

        String summary = chartData.isEmpty()
                ? "No sales data found."
                : "Top " + chartData.size() + " selling items by volume.";

        return new ReportResultDTO(
                "Top Selling Products",
                summary,
                "BAR", // Tell frontend to use a Bar Chart
                chartData
        );
    }

    @Override
    public String getType() {
        return "TOP_SELLERS";
    }
}