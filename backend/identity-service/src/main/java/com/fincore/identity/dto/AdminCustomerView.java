package com.fincore.identity.dto;

import java.time.LocalDateTime;

public record AdminCustomerView(String id, String fullName, String email,
        String applicationStatus, boolean emailVerified, LocalDateTime createdAt,
        String reviewedBy, LocalDateTime reviewedAt, String reviewReason) { }
