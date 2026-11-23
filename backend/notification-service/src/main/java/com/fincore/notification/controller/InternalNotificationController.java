package com.fincore.notification.controller;

import com.fincore.notification.dto.InternalNotificationRequest;
import com.fincore.notification.dto.NotificationResponse;
import com.fincore.notification.service.NotificationService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/internal/notifications")
public class InternalNotificationController {

    private final NotificationService notificationService;
    private final String internalApiKey;

    public InternalNotificationController(
            NotificationService notificationService,
            @Value("${app.internal-api-key}") String internalApiKey) {
        this.notificationService = notificationService;
        this.internalApiKey = internalApiKey;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(
            @RequestHeader("X-Internal-Api-Key") String suppliedKey,
            @Valid @RequestBody InternalNotificationRequest request) {

        boolean valid = MessageDigest.isEqual(
                internalApiKey.getBytes(StandardCharsets.UTF_8),
                suppliedKey.getBytes(StandardCharsets.UTF_8)
        );

        if (!valid) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid internal API key");
        }

        return notificationService.create(
                request.userId(),
                request.type(),
                request.title(),
                request.message()
        );
    }
}