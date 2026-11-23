package com.fincore.account.dto;

import java.math.BigDecimal;

public record CategorySpendingResponse(
        String category,
        BigDecimal amount,
        long transactionCount
) {}