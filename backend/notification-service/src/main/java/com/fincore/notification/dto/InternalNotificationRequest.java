package com.fincore.notification.dto;

import com.fincore.notification.entity.NotificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record InternalNotificationRequest(
        @NotNull UUID userId,
        @NotNull NotificationType type,
        @NotBlank @Size(max = 150) String title,
        @NotBlank @Size(max = 1000) String message
) {}