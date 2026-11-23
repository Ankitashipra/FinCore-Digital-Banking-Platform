package com.fincore.account.service;

import java.util.UUID;

public record RiskAlertCreatedEvent(
        UUID userId,
        String description
) {}