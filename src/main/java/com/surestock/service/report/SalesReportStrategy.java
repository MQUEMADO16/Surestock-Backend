package com.surestock.service.report;

import com.surestock.dto.report.ReportResultDTO;
import com.surestock.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SalesReportStrategy implements IReportStrategy {

    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    public ReportResultDTO generate(Long businessId) {
        var transactions = transactionRepository.findByBusinessId(businessId);

        // Logic: Sum (Sale Price * Quantity Sold) for all history
        double totalRevenue = transactions.stream()
                .mapToDouble(t -> {
                    double price = (t.getTotalPrice() != null) ? t.getTotalPrice() : 0.0;
                    int qty = (t.getQuantitySold() != null) ? t.getQuantitySold() : 0;
                    return price * qty;
                })
                .sum();

        return new ReportResultDTO(
                "Sales Revenue Report",
                "Total Lifetime Revenue: $" + String.format("%.2f", totalRevenue)
        );
    }

    @Override
    public String getType() {
        return "SALES";
    }
}