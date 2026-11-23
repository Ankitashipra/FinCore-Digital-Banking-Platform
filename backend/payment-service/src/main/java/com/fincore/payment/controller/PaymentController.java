package com.fincore.payment.controller;

import com.fincore.payment.dto.PaymentRequest;
import com.fincore.payment.dto.PaymentResponse;
import com.fincore.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody PaymentRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication,
            @RequestHeader("Authorization") String authorization
    ) {
        if (idempotencyKey.isBlank() || idempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "Idempotency-Key must contain 1 to 100 characters"
            );
        }

        String accessToken = authorization.substring(7);

        PaymentResponse response = paymentService.processPayment(
                UUID.fromString(authentication.getName()),
                request,
                accessToken,
                idempotencyKey
        );

        return ResponseEntity.status(
                "COMPLETED".equals(response.status())
                        ? HttpStatus.OK : HttpStatus.ACCEPTED
        ).body(response);
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse getPayment(
            @PathVariable UUID paymentId,
            Authentication authentication
    ) {
        return paymentService.getPayment(
                paymentId,
                UUID.fromString(authentication.getName())
        );
    }
}