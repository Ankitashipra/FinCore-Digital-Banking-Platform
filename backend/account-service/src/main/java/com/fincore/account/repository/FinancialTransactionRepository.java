package com.fincore.account.repository;

import com.fincore.account.entity.FinancialTransaction;
import com.fincore.account.entity.TransactionType;
import com.fincore.account.entity.TransactionStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FinancialTransactionRepository
        extends JpaRepository<FinancialTransaction, UUID> {

    List<FinancialTransaction> findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(
            UUID fromAccountId,
            UUID toAccountId
    );

    Optional<FinancialTransaction> findByIdempotencyKey(String idempotencyKey);

    Page<FinancialTransaction> findByFromAccountIdInOrToAccountIdIn(
            List<UUID> fromAccountIds,
            List<UUID> toAccountIds,
            Pageable pageable
    );

    @Query("""
            SELECT t FROM FinancialTransaction t
            WHERE (
                t.fromAccountId IN :accountIds
                OR t.toAccountId IN :accountIds
            )
            AND (:type IS NULL OR t.transactionType = :type)
            AND (:status IS NULL OR t.status = :status)
            """)
    Page<FinancialTransaction> findUserTransactions(
            @Param("accountIds") List<UUID> accountIds,
            @Param("type") TransactionType type,
            @Param("status") TransactionStatus status,
            Pageable pageable
    );

        @Query("""
       SELECT t FROM FinancialTransaction t
       WHERE (
        t.fromAccountId IN :accountIds
        OR t.toAccountId IN :accountIds
        )
        AND t.createdAt >= :startDate
        AND t.createdAt < :endDate
       ORDER BY t.createdAt ASC
        """)
        List<FinancialTransaction> findAnalyticsTransactions(
        @Param("accountIds") List<UUID> accountIds,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
);

        long countByFromAccountIdAndCreatedAtAfterAndStatus(
        UUID fromAccountId,
        java.time.LocalDateTime createdAt,
        TransactionStatus status
);
}