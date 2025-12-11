package com.surestock.service;

import com.surestock.dto.report.ReportResultDTO;
import com.surestock.service.report.IReportStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final Map<String, IReportStrategy> strategyMap;

    /**
     * Spring automatically injects ALL beans that implement IReportStrategy into this list.
     * We then convert that list into a Map for fast lookup by Type (e.g., "SALES" -> SalesReportStrategy).
     */
    @Autowired
    public ReportService(List<IReportStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(IReportStrategy::getType, Function.identity()));
    }

    public ReportResultDTO getReport(String type, Long businessId) {
        // Look up the strategy by the requested type (e.g., "SALES", "INVENTORY")
        IReportStrategy strategy = strategyMap.get(type.toUpperCase());

        if (strategy == null) {
            throw new IllegalArgumentException("Invalid report type: " + type);
        }

        // Execute the strategy
        return strategy.generate(businessId);
    }
}