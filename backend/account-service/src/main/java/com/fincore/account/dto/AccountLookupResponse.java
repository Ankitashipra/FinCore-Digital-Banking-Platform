package com.fincore.account.dto;
import com.fincore.account.entity.Account;
import java.util.UUID;
public record AccountLookupResponse(UUID accountId, String accountNumber, String accountType,
        String currency, String status) {
    public static AccountLookupResponse from(Account a) {
        return new AccountLookupResponse(a.getId(), a.getAccountNumber(),
                a.getAccountType().name(), a.getCurrency(), a.getStatus().name());
    }
}
