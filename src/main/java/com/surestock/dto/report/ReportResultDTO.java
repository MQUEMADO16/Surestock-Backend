package com.surestock.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReportResultDTO {
    private String title;
    private String summary;

    // You can add a Map<String, Object> details; here later
    // if you want to send charts/graphs data to React.
}