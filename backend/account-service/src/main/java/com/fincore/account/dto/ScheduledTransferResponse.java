package com.fincore.account.dto;

import com.fincore.account.entity.ScheduledTransfer;
import com.fincore.account.entity.ScheduledTransferStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduledTransferResponse(
        UUID id,
        UUID fromAccountId,
        UUID toAccountId,
        BigDecimal amount,
        String currency,
        String description,
        LocalDateTime scheduledAt,
        ScheduledTransferStatus status,
        LocalDateTime createdAt
) {
    public static ScheduledTransferResponse from(ScheduledTransfer transfer) {
        return new ScheduledTransferResponse(
                transfer.getId(),
                transfer.getFromAccountId(),
                transfer.getToAccountId(),
                transfer.getAmount(),
                transfer.getCurrency(),
                transfer.getDescription(),
                transfer.getScheduledAt(),
                transfer.getStatus(),
                transfer.getCreatedAt()
        );
    }
}