package com.surestock.service.report;

import com.surestock.model.Product;
import com.surestock.model.SalesTransaction;
import com.surestock.repository.ProductRepository;
import com.surestock.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calculates total profit margin by deducting cost from sale price for all transactions.
 */
@Component
public class ProfitMarginReportStrategy implements IReportStrategy {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Map<String, Object> generate(Long businessId, Map<String, String> parameters) {

        LocalDateTime endDate = parameters.containsKey("endDate")
                ? LocalDateTime.parse(parameters.get("endDate"))
                : LocalDateTime.now();
        LocalDateTime startDate = parameters.containsKey("startDate")
                ? LocalDateTime.parse(parameters.get("startDate"))
                : endDate.minusDays(90); // Default: last 90 days

        List<SalesTransaction> transactions = transactionRepository.findByBusinessIdAndTimestampBetween(businessId, startDate, endDate);

        // Fetch all products to map Product ID to Cost (necessary for margin calculation)
        Map<Long, Product> productMap = productRepository.findByBusinessId(businessId).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        double totalProfit = 0.0;
        Map<Long, Double> profitByProduct = new HashMap<>();

        for (SalesTransaction tx : transactions) {
            Product product = productMap.get(tx.getProduct().getId());
            if (product != null && product.getCost() != null) {
                // Profit = (Sale Price - Unit Cost) * Quantity Sold
                double unitProfit = tx.getTotalPrice() - product.getCost();
                double transactionProfit = unitProfit * tx.getQuantitySold();

                totalProfit += transactionProfit;
                profitByProduct.merge(tx.getProduct().getId(), transactionProfit, Double::sum);
            }
        }

        // Format the results (Top 5 profitable items)
        List<Map<String, Object>> topProfits = profitByProduct.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .map(entry -> {
                    Product product = productMap.get(entry.getKey());
                    Map<String, Object> item = new HashMap<>();
                    item.put("productId", entry.getKey());
                    item.put("productName", (product != null) ? product.getName() : "Unknown");
                    item.put("totalProfit", String.format("%.2f", entry.getValue()));
                    return item;
                })
                .collect(Collectors.toList());


        Map<String, Object> report = new HashMap<>();
        report.put("title", "Profit Margin Report");
        report.put("summary", String.format("Total Gross Profit Margin (%s to %s): $%.2f",
                startDate.toLocalDate(), endDate.toLocalDate(), totalProfit));
        report.put("totalProfit", totalProfit);
        report.put("topProfitableItems", topProfits);
        report.put("type", getType());
        return report;
    }

    @Override
    public String getType() {
        return "PROFIT";
    }
}