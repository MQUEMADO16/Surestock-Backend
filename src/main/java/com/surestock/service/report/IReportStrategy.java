package com.surestock.service.report;

import com.surestock.dto.report.ReportResultDTO;

public interface IReportStrategy {
    /**
     * Generates a report for a specific business.
     */
    ReportResultDTO generate(Long businessId);

    /**
     * Returns the unique type name for this strategy (e.g., "INVENTORY").
     * This is how the Factory knows which one to pick.
     */
    String getType();
}