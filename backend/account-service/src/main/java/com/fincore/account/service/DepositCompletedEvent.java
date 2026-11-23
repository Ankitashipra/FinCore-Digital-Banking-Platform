package com.fincore.account.service;

import java.math.BigDecimal;
import java.util.UUID;

public record DepositCompletedEvent(
        UUID userId,
        BigDecimal amount,
        String currency
) {}