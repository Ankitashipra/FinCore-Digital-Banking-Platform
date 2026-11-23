package com.fincore.payment.service;

import com.fincore.payment.dto.PaymentRequest;
import com.fincore.payment.entity.Payment;
import com.fincore.payment.entity.PaymentStatus;
import com.fincore.payment.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentPersistenceService {

    private final PaymentRepository paymentRepository;

    public PaymentPersistenceService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment create(
            UUID ownerUserId,
            PaymentRequest request,
            String idempotencyKey
    ) {
        Payment payment = new Payment();
        payment.setOwnerUserId(ownerUserId);
        payment.setSourceAccountId(request.sourceAccountId());
        payment.setDestinationAccountId(request.destinationAccountId());
        payment.setAmount(request.amount());
        payment.setCurrency(request.currency().toUpperCase());
        payment.setDescription(request.description());
        payment.setIdempotencyKey(idempotencyKey);
        payment.setStatus(PaymentStatus.PENDING);

        return paymentRepository.saveAndFlush(payment);
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findByIdempotencyKey(
            UUID ownerUserId,
            String idempotencyKey
    ) {
        return paymentRepository.findByOwnerUserIdAndIdempotencyKey(
                ownerUserId, idempotencyKey
        );
    }

    @Transactional(readOnly = true)
    public Optional<Payment> findOwnedPayment(UUID id, UUID ownerUserId) {
        return paymentRepository.findByIdAndOwnerUserId(id, ownerUserId);
    }

    @Transactional
    public Payment markProcessing(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow();
        payment.setStatus(PaymentStatus.PROCESSING);
        return payment;
    }

    @Transactional
    public Payment markCompleted(
            UUID paymentId,
            UUID accountTransactionId,
            String actualCurrency
    ) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow();

        payment.setAccountTransactionId(accountTransactionId);
        if (actualCurrency != null && !actualCurrency.isBlank()) {
            payment.setCurrency(actualCurrency);
        }
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setFailureReason(null);
        return payment;
    }

    @Transactional
    public Payment markFailed(UUID paymentId, String reason) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow();

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(
                reason == null ? "Transfer rejected by Account Service"
                        : reason.substring(0, Math.min(reason.length(), 500))
        );
        return payment;
    }
}