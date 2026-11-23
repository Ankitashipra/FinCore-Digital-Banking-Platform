package com.fincore.payment.service;

import com.fincore.payment.client.AccountServiceClient;
import com.fincore.payment.dto.PaymentRequest;
import com.fincore.payment.dto.PaymentResponse;
import com.fincore.payment.entity.Payment;
import com.fincore.payment.entity.PaymentStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;

import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentPersistenceService persistenceService;
    private final AccountServiceClient accountServiceClient;

    public PaymentService(
            PaymentPersistenceService persistenceService,
            AccountServiceClient accountServiceClient
    ) {
        this.persistenceService = persistenceService;
        this.accountServiceClient = accountServiceClient;
    }

    public PaymentResponse processPayment(
            UUID ownerUserId,
            PaymentRequest request,
            String accessToken,
            String idempotencyKey
    ) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new IllegalArgumentException(
                    "Source and destination accounts must be different"
            );
        }

        Payment payment = persistenceService.findByIdempotencyKey(
                ownerUserId, idempotencyKey
        ).orElse(null);

        if (payment == null) {
            try {
                payment = persistenceService.create(
                        ownerUserId, request, idempotencyKey
                );
            } catch (DataIntegrityViolationException exception) {
                // Another request may have created this idempotency key.
                payment = persistenceService.findByIdempotencyKey(
                        ownerUserId, idempotencyKey
                ).orElseThrow(() -> exception);
            }
        } else {
            validateSameRequest(payment, request);
        }

        if (payment.getStatus() == PaymentStatus.COMPLETED
                || payment.getStatus() == PaymentStatus.FAILED) {
            return toResponse(payment);
        }

        // The payment UUID gives Account Service a stable downstream key.
        // Retries of this payment always reuse the same key.
        String downstreamKey = "payment-" + payment.getId();

        try {
            persistenceService.markProcessing(payment.getId());

            JsonNode result = accountServiceClient.executeTransfer(
                    payment.getSourceAccountId(),
                    payment.getDestinationAccountId(),
                    payment.getAmount(),
                    payment.getDescription(),
                    accessToken,
                    downstreamKey
            );

            if (result == null) {
                return toResponse(
                        persistenceService.markProcessing(payment.getId())
                );
            }

            String downstreamStatus = result.path("status").asText("");
            if ("COMPLETED".equalsIgnoreCase(downstreamStatus)) {
                String transactionId = result.path("transactionId").asText("");
                UUID accountTransactionId = transactionId.isBlank()
                        ? null : UUID.fromString(transactionId);

                String actualCurrency = result.path("currency").asText("");

                return toResponse(persistenceService.markCompleted(
                        payment.getId(),
                        accountTransactionId,
                        actualCurrency
                ));
            }

            if ("FAILED".equalsIgnoreCase(downstreamStatus)) {
                return toResponse(persistenceService.markFailed(
                        payment.getId(),
                        "Account Service reported a failed transfer"
                ));
            }

            // Unknown downstream status: preserve the payment for safe retry.
            return toResponse(
                    persistenceService.markProcessing(payment.getId())
            );

        } catch (HttpClientErrorException exception) {
            // A 4xx response indicates that Account Service rejected the request.
            return toResponse(persistenceService.markFailed(
                    payment.getId(),
                    "Account Service rejected the transfer (HTTP "
                            + exception.getStatusCode().value() + ")"
            ));
        } catch (RestClientException exception) {
            // A timeout or server error may occur after the transfer committed.
            // Keep PROCESSING; a retry must use the same downstream key.
            return toResponse(
                    persistenceService.markProcessing(payment.getId())
            );
        }
    }

    public PaymentResponse getPayment(UUID paymentId, UUID ownerUserId) {
    Payment payment = persistenceService.findOwnedPayment(
            paymentId, ownerUserId
    ).orElseThrow(() ->
            new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Payment not found"
            )
    );

    return toResponse(payment);
}

    private void validateSameRequest(
            Payment existing,
            PaymentRequest request
    ) {
        boolean sameRequest =
                existing.getSourceAccountId().equals(request.sourceAccountId())
                && existing.getDestinationAccountId().equals(
                        request.destinationAccountId())
                && existing.getAmount().compareTo(request.amount()) == 0
                && existing.getCurrency().equalsIgnoreCase(request.currency());

        if (!sameRequest) {
            throw new IllegalArgumentException(
                    "Idempotency key has already been used with different payment details"
            );
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getSourceAccountId(),
                payment.getDestinationAccountId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus().name(),
                payment.getDescription(),
                payment.getCreatedAt()
        );
    }
}