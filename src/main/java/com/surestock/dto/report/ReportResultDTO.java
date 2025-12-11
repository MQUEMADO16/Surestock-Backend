package com.surestock.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ReportResultDTO {
    private String title;
    private String summary;       // A brief text summary (e.g., "Total Revenue: $500")
    private String chartType;     // Options: "BAR", "LINE", "PIE", "TABLE", "NONE"
    private List<Map<String, Object>> data; // The raw data points for the graph
}