
package com.fincore.account.dto;

import com.fincore.account.entity.TransactionStatus;
import com.fincore.account.entity.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransferResponse(
        UUID transactionId,
        UUID fromAccountId,
        UUID toAccountId,
        BigDecimal amount,
        String currency,
        TransactionType transactionType,
        TransactionStatus status,
        String description,
        LocalDateTime createdAt
) {}
