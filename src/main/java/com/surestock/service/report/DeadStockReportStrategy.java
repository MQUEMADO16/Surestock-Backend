package com.surestock.service.report;

import com.surestock.model.Product;
import com.surestock.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Identifies products that have positive quantity but zero sales in the last X days/months.
 */
@Component
public class DeadStockReportStrategy implements IReportStrategy {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Map<String, Object> generate(Long businessId, Map<String, String> parameters) {

        // Default check: 60 days of inactivity
        LocalDateTime startDate = parameters.containsKey("startDate")
                ? LocalDateTime.parse(parameters.get("startDate"))
                : LocalDateTime.now().minusDays(60);

        // Uses the custom repository query to find items not sold since startDate
        List<Product> deadStockCandidates = productRepository.findDeadStockCandidates(businessId, startDate);

        String summary = String.format("Found %d items that have not sold since %s.", deadStockCandidates.size(), startDate.toLocalDate());

        List<Map<String, Object>> details = deadStockCandidates.stream()
                .map(p -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("productId", p.getId());
                    item.put("name", p.getName());
                    item.put("currentQuantity", p.getQuantity());
                    item.put("inventoryValue", String.format("%.2f", p.getPrice() * p.getQuantity()));
                    return item;
                })
                .collect(Collectors.toList());

        Map<String, Object> report = new HashMap<>();
        report.put("title", "Dead Stock Report");
        report.put("summary", summary);
        report.put("recommendation", "Consider running promotions or marking down these items to free up capital and shelf space.");
        report.put("data", details);
        report.put("type", getType());
        return report;
    }

    @Override
    public String getType() {
        return "DEAD_STOCK";
    }
}