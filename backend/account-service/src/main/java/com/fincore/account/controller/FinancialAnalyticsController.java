package com.fincore.account.controller;

import com.fincore.account.dto.FinancialAnalyticsResponse;
import com.fincore.account.service.FinancialAnalyticsService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
public class FinancialAnalyticsController {

    private final FinancialAnalyticsService analyticsService;

    public FinancialAnalyticsController(
            FinancialAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/financial-summary")
    public FinancialAnalyticsResponse getFinancialSummary(
            @RequestParam(defaultValue = "6") int months,
            Authentication authentication) {

        return analyticsService.getAnalytics(
                authentication.getName(), months);
    }
}