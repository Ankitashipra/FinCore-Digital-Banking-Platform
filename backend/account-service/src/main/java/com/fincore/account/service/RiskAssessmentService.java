package com.fincore.account.service;

import com.fincore.account.entity.*;
import com.fincore.account.repository.FinancialTransactionRepository;
import com.fincore.account.repository.RiskAlertRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;


@Service
public class RiskAssessmentService {

    private final FinancialTransactionRepository transactionRepository;
    private final RiskAlertRepository alertRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${fincore.risk.large-transfer-threshold:50000}")
    private BigDecimal largeTransferThreshold;

    @Value("${fincore.risk.velocity-limit:3}")
    private int velocityLimit;

    public RiskAssessmentService(
            FinancialTransactionRepository transactionRepository,
            RiskAlertRepository alertRepository,ApplicationEventPublisher eventPublisher) {
        this.transactionRepository = transactionRepository;
        this.alertRepository = alertRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void assessTransfer(
            UUID ownerId,
            UUID fromAccountId,
            BigDecimal amount) {

        if (amount.compareTo(largeTransferThreshold) >= 0) {
            createAlert(
                    ownerId,
                    fromAccountId,
                    amount,
                    RiskAlertSeverity.HIGH,
                    "LARGE_TRANSFER",
                    "Transfer meets or exceeds the configured large-transfer threshold."
            );
        }

        LocalDateTime tenMinutesAgo = LocalDateTime.now().minusMinutes(10);

        long recentTransfers =
                transactionRepository
                        .countByFromAccountIdAndCreatedAtAfterAndStatus(
                                fromAccountId,
                                tenMinutesAgo,
                                TransactionStatus.COMPLETED
                        );

        if (recentTransfers >= velocityLimit) {
            createAlert(
                    ownerId,
                    fromAccountId,
                    amount,
                    RiskAlertSeverity.CRITICAL,
                    "HIGH_TRANSFER_VELOCITY",
                    "Multiple completed outgoing transfers detected within ten minutes."
            );
        }
    }

    private void createAlert(
            UUID ownerId,
            UUID accountId,
            BigDecimal amount,
            RiskAlertSeverity severity,
            String ruleCode,
            String description) {

        RiskAlert alert = new RiskAlert();
        alert.setOwnerUserId(ownerId);
        alert.setAccountId(accountId);
        alert.setAmount(amount);
        alert.setSeverity(severity);
        alert.setRuleCode(ruleCode);
        alert.setDescription(description);
        alert.setStatus(RiskAlertStatus.OPEN);

  alertRepository.save(alert);

eventPublisher.publishEvent(
        new RiskAlertCreatedEvent(
                ownerId,
                alert.getDescription()
        )
);
    }
}