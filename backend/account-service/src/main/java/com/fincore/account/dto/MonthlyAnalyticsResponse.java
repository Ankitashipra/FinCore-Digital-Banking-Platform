package com.fincore.account.dto;

import java.math.BigDecimal;

public record MonthlyAnalyticsResponse(
        String month,
        BigDecimal income,
        BigDecimal expenses,
        BigDecimal netCashFlow,
        long transactionCount
) {}