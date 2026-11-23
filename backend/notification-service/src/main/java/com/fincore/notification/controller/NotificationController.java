package com.fincore.notification.controller;

import com.fincore.notification.dto.NotificationResponse;
import com.fincore.notification.service.NotificationService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping
    public List<NotificationResponse> getMyNotifications(
            Authentication authentication) {

        return service.getMyNotifications(
                UUID.fromString(authentication.getName()));
    }

    @GetMapping("/unread-count")
    public long getUnreadCount(Authentication authentication) {
        return service.getUnreadCount(
                UUID.fromString(authentication.getName()));
    }

    @PatchMapping("/{id}/read")
    public void markAsRead(
            @PathVariable UUID id,
            Authentication authentication) {

        service.markAsRead(
                UUID.fromString(authentication.getName()), id);
    }

    @PatchMapping("/read-all")
    public void markAllAsRead(Authentication authentication) {
        service.markAllAsRead(
                UUID.fromString(authentication.getName()));
    }
}