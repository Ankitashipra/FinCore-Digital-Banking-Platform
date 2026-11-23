package com.fincore.payment.client;

import tools.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
public class AccountServiceClient {

    private final RestClient restClient;

    public AccountServiceClient(
            @Value("${app.account.base-url}") String baseUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public JsonNode executeTransfer(
            UUID sourceAccountId,
            UUID destinationAccountId,
            BigDecimal amount,
            String description,
            String accessToken,
            String idempotencyKey) {

        Map<String, Object> request = Map.of(
                "fromAccountId", sourceAccountId,
                "toAccountId", destinationAccountId,
                "amount", amount,
                "description",
                description == null ? "Payment transfer" : description
        );

        return restClient.post()
                .uri("/api/v1/transfers")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + accessToken
                )
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(JsonNode.class);
    }
}