package com.surestock.service.report;

import java.util.Map;

public interface IReportStrategy {
    /**
     * Generates a report for a specific business, accepting parameters for filtering
     * (e.g., startDate, endDate).
     * The return type is a flexible map to accommodate complex data structures (lists, totals, etc.).
     *
     * @param businessId The unique identifier of the business tenant.
     * @param parameters A map containing filtering parameters (e.g., "startDate").
     */
    Map<String, Object> generate(Long businessId, Map<String, String> parameters);

    /**
     * Returns the unique type name for this strategy (e.g., "INVENTORY").
     */
    String getType();
}