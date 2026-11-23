package com.fincore.notification.service;

import com.fincore.notification.dto.NotificationResponse;
import com.fincore.notification.entity.Notification;
import com.fincore.notification.entity.NotificationType;
import com.fincore.notification.repository.NotificationRepository;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(
            NotificationRepository repository,
            SimpMessagingTemplate messagingTemplate) {
        this.repository = repository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public NotificationResponse create(
            UUID userId,
            NotificationType type,
            String title,
            String message) {

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);

        Notification saved = repository.save(notification);

        NotificationResponse response =
                NotificationResponse.from(saved);

        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                response
        );

        return response;
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(UUID userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @Transactional
    public void markAsRead(UUID userId, UUID notificationId) {

        Notification notification = repository
                .findByIdAndUserId(notificationId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Notification not found"));

        notification.setRead(true);
    }

    @Transactional
    public void markAllAsRead(UUID userId) {

        repository.findByUserIdOrderByCreatedAtDesc(userId)
                .forEach(notification -> notification.setRead(true));
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(UUID userId) {
        return repository.countByUserIdAndReadFalse(userId);
    }
}