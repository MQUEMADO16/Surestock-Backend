package com.surestock.service;

import com.surestock.dto.report.ReportResultDTO;
import com.surestock.service.report.IReportStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final Map<String, IReportStrategy> strategies;

    public ReportService(@org.jetbrains.annotations.NotNull List<IReportStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(IReportStrategy::getType, s -> s));
    }

    public ReportResultDTO getReport(String type, Long businessId) {
        IReportStrategy strategy = strategies.get(type);

        if (strategy == null) {
            throw new IllegalArgumentException("Invalid report type: " + type);
        }

        return strategy.generate(businessId);
    }
}