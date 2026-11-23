package com.fincore.account.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "risk_alerts",
    indexes = {
        @Index(name = "idx_risk_owner_created",
               columnList = "owner_user_id, created_at"),
        @Index(name = "idx_risk_status",
               columnList = "status")
    }
)
public class RiskAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskAlertSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskAlertStatus status = RiskAlertStatus.OPEN;

    @Column(nullable = false, length = 80)
    private String ruleCode;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }

    public UUID getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(UUID ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    public UUID getAccountId() { return accountId; }
    public void setAccountId(UUID accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public RiskAlertSeverity getSeverity() { return severity; }
    public void setSeverity(RiskAlertSeverity severity) {
        this.severity = severity;
    }

    public RiskAlertStatus getStatus() { return status; }
    public void setStatus(RiskAlertStatus status) {
        this.status = status;
    }

    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
}