package com.fincore.account.service;
import java.math.BigDecimal;
import java.util.UUID;
public record TransferCompletedEvent(UUID senderUserId, UUID recipientUserId,
        BigDecimal amount, String currency, String reference) {}
