package com.fincore.account.repository;

import com.fincore.account.ledger.LedgerEntry;
import com.fincore.account.ledger.LedgerEntryType;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository
        extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(UUID accountId);

    List<LedgerEntry> findByTransactionId(UUID transactionId);

    boolean existsByTransactionIdAndAccountIdAndEntryType(
        UUID transactionId,
        UUID accountId,
        LedgerEntryType entryType
);
}