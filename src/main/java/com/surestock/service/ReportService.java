package com.surestock.service;

import com.surestock.service.report.IReportStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final Map<String, IReportStrategy> strategies;

    public ReportService(List<IReportStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(IReportStrategy::getType, s -> s));
    }

    /**
     * Retrieves the correct report strategy and executes it with the given parameters.
     * * @param type The unique type key of the report (e.g., "PROFIT").
     * @param businessId The ID of the logged-in business.
     * @param parameters Filtering parameters (e.g., startDate, limit).
     * @return A Map containing the structured report data.
     */
    public Map<String, Object> getReport(String type, Long businessId, Map<String, String> parameters) {
        IReportStrategy strategy = strategies.get(type);

        if (strategy == null) {
            throw new IllegalArgumentException("Invalid report type: " + type);
        }

        return strategy.generate(businessId, parameters);
    }
}