package com.restaurant.reporting;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ReportingService {

    public DailySummaryResponse getDailySummary(String tenantId, String propertyId) {
        return new DailySummaryResponse(propertyId, 142, BigDecimal.valueOf(45230), 18, 9);
    }
}
