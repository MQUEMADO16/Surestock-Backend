package com.surestock.service.report;

import com.surestock.model.Product;
import com.surestock.repository.ProductRepository;
import com.surestock.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ranks products by sales volume (quantity sold) over a specified period using a specialized repository query.
 */
@Component
public class TopSellersReportStrategy implements IReportStrategy {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Map<String, Object> generate(Long businessId, Map<String, String> parameters) {

        // Default to checking sales from the last 30 days
        LocalDateTime startDate = parameters.containsKey("startDate")
                ? LocalDateTime.parse(parameters.get("startDate"))
                : LocalDateTime.now().minusDays(30);

        // Use the specialized repository query for efficient aggregation
        List<Map<String, Object>> topSellingRaw = transactionRepository.findTopSellingProducts(businessId, startDate);

        // Fetch all products to get product names
        Map<Long, Product> productMap = productRepository.findByBusinessId(businessId).stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        int totalProducts = topSellingRaw.size();

        // Format the output
        List<Map<String, Object>> topList = topSellingRaw.stream()
                .map(raw -> {
                    Long productId = (Long) raw.get("productId");
                    // The repository returns BigInteger for SUM/COUNT, cast to Number
                    Number totalQty = (Number) raw.get("totalQuantitySold");

                    Map<String, Object> item = new HashMap<>();
                    item.put("productId", productId);
                    item.put("productName", productMap.getOrDefault(productId, new Product()).getName());
                    item.put("totalQuantitySold", totalQty.longValue());
                    return item;
                })
                .limit(10) // Show top 10
                .collect(Collectors.toList());

        Map<String, Object> report = new HashMap<>();
        report.put("title", "Top Sellers Report");
        report.put("summary", String.format("Analyzed %d products sold since %s.", totalProducts, startDate.toLocalDate()));
        report.put("data", topList);
        report.put("type", getType());
        return report;
    }

    @Override
    public String getType() {
        return "TOP_SELLERS";
    }
}