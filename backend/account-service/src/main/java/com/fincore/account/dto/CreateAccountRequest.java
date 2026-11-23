package com.fincore.account.dto;

import com.fincore.account.entity.AccountType;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(

    @NotNull(message = "Account type is required")
    AccountType accountType

) {}