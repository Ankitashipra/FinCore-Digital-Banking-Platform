package com.fincore.account.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "beneficiaries",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_beneficiary_owner_account",
        columnNames = {"owner_user_id", "beneficiary_account_id"}
    ),
    indexes = @Index(name = "idx_beneficiary_owner", columnList = "owner_user_id")
)
public class Beneficiary {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "owner_user_id", nullable = false)
    private UUID ownerUserId;

    @Column(name = "beneficiary_account_id", nullable = false)
    private UUID beneficiaryAccountId;

    @Column(nullable = false, length = 80)
    private String nickname;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public UUID getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(UUID ownerUserId) { this.ownerUserId = ownerUserId; }
    public UUID getBeneficiaryAccountId() { return beneficiaryAccountId; }
    public void setBeneficiaryAccountId(UUID beneficiaryAccountId) {
        this.beneficiaryAccountId = beneficiaryAccountId;
    }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
