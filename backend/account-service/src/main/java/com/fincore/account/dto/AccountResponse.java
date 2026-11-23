package com.fincore.account.dto;

import com.fincore.account.entity.Account;
import com.fincore.account.entity.AccountStatus;
import com.fincore.account.entity.AccountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        AccountType accountType,
        String currency,
        BigDecimal balance,
        AccountStatus status,
        LocalDateTime createdAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getCurrency(),
                account.getBalance(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }
}