package com.surestock.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO used for filtering reports by start and end timestamp.
 */
@Data
public class DateRangeDTO {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}