package com.fincore.payment.repository;

import com.fincore.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByOwnerUserIdAndIdempotencyKey(
            UUID ownerUserId,
            String idempotencyKey
    );

    Optional<Payment> findByIdAndOwnerUserId(
            UUID id,
            UUID ownerUserId
    );
}