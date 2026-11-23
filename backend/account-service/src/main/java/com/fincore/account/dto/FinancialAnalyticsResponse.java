package com.fincore.account.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinancialAnalyticsResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netCashFlow,
        long totalTransactions,
        List<MonthlyAnalyticsResponse> monthlyAnalytics,
        List<CategorySpendingResponse> categorySpending
) {}