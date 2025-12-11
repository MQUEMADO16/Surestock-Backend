package com.surestock.service.report;

import com.surestock.dto.report.ReportResultDTO;
import com.surestock.model.SalesTransaction;
import com.surestock.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProfitMarginReportStrategy implements IReportStrategy {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public ReportResultDTO generate(Long businessId) {
        List<SalesTransaction> transactions = transactionRepository.findByBusinessId(businessId);

        double totalRevenue = 0.0;
        double totalCost = 0.0;

        for (SalesTransaction t : transactions) {
            double revenue = (t.getTotalPrice() != null) ? t.getTotalPrice() : 0.0;
            Double productCost = (t.getProduct() != null && t.getProduct().getCost() != null)
                    ? t.getProduct().getCost()
                    : 0.0;
            int qty = (t.getQuantitySold() != null) ? t.getQuantitySold() : 0;

            totalRevenue += revenue;
            totalCost += (productCost * qty);
        }

        double totalProfit = totalRevenue - totalCost;

        // Build Pie Chart Data: Slice 1 (Cost), Slice 2 (Profit)
        List<Map<String, Object>> chartData = new ArrayList<>();

        Map<String, Object> costSlice = new HashMap<>();
        costSlice.put("name", "Cost of Goods");
        costSlice.put("value", totalCost);
        chartData.add(costSlice);

        Map<String, Object> profitSlice = new HashMap<>();
        profitSlice.put("name", "Net Profit");
        profitSlice.put("value", totalProfit);
        chartData.add(profitSlice);

        double margin = (totalRevenue > 0) ? (totalProfit / totalRevenue) * 100 : 0.0;
        String summary = String.format("Net Profit: $%.2f (Margin: %.1f%%)", totalProfit, margin);

        return new ReportResultDTO(
                "Profit Margin Analysis",
                summary,
                "PIE",
                chartData
        );
    }

    @Override
    public String getType() {
        return "PROFIT";
    }
}