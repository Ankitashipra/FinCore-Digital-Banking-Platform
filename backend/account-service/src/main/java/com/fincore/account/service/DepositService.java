package com.fincore.account.service;

import com.fincore.account.dto.DepositRequest;
import com.fincore.account.dto.DepositResponse;
import com.fincore.account.entity.*;
import com.fincore.account.ledger.LedgerEntry;
import com.fincore.account.repository.LedgerEntryRepository;
import com.fincore.account.ledger.LedgerEntryType;
import com.fincore.account.repository.AccountRepository;
import com.fincore.account.repository.FinancialTransactionRepository;
import jakarta.transaction.Transactional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class DepositService {

    private final AccountRepository accountRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final ApplicationEventPublisher eventPublisher;
    
    public DepositService(
            AccountRepository accountRepository,
            FinancialTransactionRepository transactionRepository,
            LedgerEntryRepository ledgerEntryRepository,ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public DepositResponse deposit(
            String ownerUserId,
            UUID accountId,
            DepositRequest request,
            String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {
            throw new IllegalArgumentException("A valid Idempotency-Key is required");
        }

        if (request.amount() == null
                || request.amount().compareTo(new BigDecimal("0.01")) < 0
                || request.amount().scale() > 2
                || request.amount().precision() > 19) {
            throw new IllegalArgumentException("Invalid deposit amount");
        }

        UUID userId = UUID.fromString(ownerUserId);

        Account account = accountRepository
                .findByIdAndOwnerUserIdForUpdate(accountId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active");
        }

        var existing = transactionRepository.findByIdempotencyKey(idempotencyKey);

        if (existing.isPresent()) {
            FinancialTransaction previous = existing.get();

            if (previous.getTransactionType() != TransactionType.DEPOSIT
                    || !accountId.equals(previous.getToAccountId())
                    || request.amount().compareTo(previous.getAmount()) != 0) {
                throw new IllegalArgumentException(
                        "Idempotency key was already used for another operation");
            }

            return toResponse(previous);
        }

        account.setBalance(account.getBalance().add(request.amount()));

        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setFromAccountId(null);
        transaction.setToAccountId(accountId);
        transaction.setAmount(request.amount());
        transaction.setCurrency(account.getCurrency());
        transaction.setTransactionType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription(
                request.description() == null
                        ? "Demo deposit"
                        : request.description());
        transaction.setIdempotencyKey(idempotencyKey);

        transaction = transactionRepository.saveAndFlush(transaction);

        LedgerEntry entry = new LedgerEntry();
        entry.setTransactionId(transaction.getId());
        entry.setAccountId(accountId);
        entry.setEntryType(LedgerEntryType.CREDIT);
        entry.setAmount(request.amount());
        entry.setCurrency(account.getCurrency());

        ledgerEntryRepository.save(entry);
        eventPublisher.publishEvent(
        new DepositCompletedEvent(
                userId,
                request.amount(),
                account.getCurrency()
        )
);

        return toResponse(transaction);
    }

    private DepositResponse toResponse(FinancialTransaction transaction) {
        return new DepositResponse(
                transaction.getId(),
                transaction.getToAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getStatus().name(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}