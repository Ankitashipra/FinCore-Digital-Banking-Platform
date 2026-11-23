package com.fincore.account.repository;

import com.fincore.account.entity.ScheduledTransfer;
import com.fincore.account.entity.ScheduledTransferStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduledTransferRepository
        extends JpaRepository<ScheduledTransfer, UUID> {

    List<ScheduledTransfer> findByOwnerUserIdOrderByScheduledAtAsc(
            UUID ownerUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT s FROM ScheduledTransfer s
            WHERE s.id = :id
            """)
    Optional<ScheduledTransfer> findByIdForUpdate(
            @Param("id") UUID id);

    @Query("""
            SELECT s.id FROM ScheduledTransfer s
            WHERE s.status = :status
            AND s.scheduledAt <= :now
            ORDER BY s.scheduledAt ASC
            """)
    List<UUID> findDueTransferIds(
            @Param("status") ScheduledTransferStatus status,
            @Param("now") LocalDateTime now);
}