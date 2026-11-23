
package com.fincore.account.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class NotificationClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public NotificationClient(
            @Value("${app.notification.base-url}") String baseUrl,
            @Value("${app.notification.internal-api-key}") String internalApiKey) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();

        this.internalApiKey = internalApiKey;
    }

    public void send(UUID userId, String type, String title, String message) {
        restClient.post()
                .uri("/internal/notifications")
                .header("X-Internal-Api-Key", internalApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new NotificationRequest(userId, type, title, message))
                .retrieve()
                .toBodilessEntity();
    }

    private record NotificationRequest(
            UUID userId,
            String type,
            String title,
            String message
    ) {}
}
