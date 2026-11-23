package com.fincore.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record DepositResponse(
        UUID transactionId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String status,
        String description,
        LocalDateTime createdAt
) {}