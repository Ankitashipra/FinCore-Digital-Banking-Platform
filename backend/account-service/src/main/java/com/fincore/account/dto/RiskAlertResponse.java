package com.fincore.account.dto;

import com.fincore.account.entity.RiskAlert;
import com.fincore.account.entity.RiskAlertSeverity;
import com.fincore.account.entity.RiskAlertStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record RiskAlertResponse(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        RiskAlertSeverity severity,
        RiskAlertStatus status,
        String ruleCode,
        String description,
        LocalDateTime createdAt
) {
    public static RiskAlertResponse from(RiskAlert alert) {
        return new RiskAlertResponse(
                alert.getId(),
                alert.getAccountId(),
                alert.getAmount(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getRuleCode(),
                alert.getDescription(),
                alert.getCreatedAt()
        );
    }
}