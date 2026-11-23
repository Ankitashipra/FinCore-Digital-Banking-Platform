package com.fincore.account.repository;

import com.fincore.account.entity.RiskAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskAlertRepository
        extends JpaRepository<RiskAlert, UUID> {

    List<RiskAlert> findByOwnerUserIdOrderByCreatedAtDesc(UUID ownerUserId);

    Optional<RiskAlert> findByIdAndOwnerUserId(
            UUID id, UUID ownerUserId);
}