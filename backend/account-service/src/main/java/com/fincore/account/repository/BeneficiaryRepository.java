package com.fincore.account.repository;

import com.fincore.account.entity.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BeneficiaryRepository
        extends JpaRepository<Beneficiary, UUID> {

    List<Beneficiary> findByOwnerUserIdOrderByCreatedAtDesc(UUID ownerUserId);

    Optional<Beneficiary> findByIdAndOwnerUserId(
            UUID id, UUID ownerUserId);

    boolean existsByOwnerUserIdAndBeneficiaryAccountId(
            UUID ownerUserId, UUID beneficiaryAccountId);
}