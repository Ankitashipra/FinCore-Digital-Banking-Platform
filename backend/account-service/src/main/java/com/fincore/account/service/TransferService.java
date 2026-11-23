
package com.fincore.account.service;

import com.fincore.account.dto.TransferRequest;
import com.fincore.account.dto.TransferResponse;
import com.fincore.account.entity.Account;
import com.fincore.account.entity.AccountStatus;
import com.fincore.account.entity.FinancialTransaction;
import com.fincore.account.entity.TransactionStatus;
import com.fincore.account.entity.TransactionType;
import com.fincore.account.exception.ResourceNotFoundException;
import com.fincore.account.ledger.LedgerEntry;
import com.fincore.account.ledger.LedgerEntryType;
import com.fincore.account.repository.AccountRepository;
import com.fincore.account.repository.FinancialTransactionRepository;
import com.fincore.account.repository.LedgerEntryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;


@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final FinancialTransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final RiskAssessmentService riskAssessmentService;
    private final ApplicationEventPublisher eventPublisher;

    public TransferService(
            AccountRepository accountRepository,
            FinancialTransactionRepository transactionRepository,
            LedgerEntryRepository ledgerEntryRepository,
        RiskAssessmentService riskAssessmentService,ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.riskAssessmentService = riskAssessmentService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public TransferResponse transfer(
            String ownerUserId,
            TransferRequest request,
            String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "A valid Idempotency-Key header is required"
            );
        }

        UUID userId = UUID.fromString(ownerUserId);
        UUID fromId = request.fromAccountId();
        UUID toId = request.toAccountId();
        BigDecimal amount = request.amount();

        if (fromId == null || toId == null || fromId.equals(toId)) {
            throw new IllegalArgumentException(
                    "Source and destination accounts must be different"
            );
        }

        if (amount == null || amount.scale() > 2
                || amount.compareTo(new BigDecimal("0.01")) < 0
                || amount.precision() > 19) {
            throw new IllegalArgumentException(
                    "Amount must be positive and have at most two decimal places"
            );
        }

        FinancialTransaction existing =
                transactionRepository.findByIdempotencyKey(idempotencyKey)
                        .orElse(null);

        if (existing != null) {
            if (!existing.getFromAccountId().equals(fromId)
                    || !existing.getToAccountId().equals(toId)
                    || existing.getAmount().compareTo(amount) != 0) {
                throw new IllegalArgumentException(
                        "Idempotency key was already used for a different request"
                );
            }
            return toResponse(existing);
        }

        UUID firstId = fromId.compareTo(toId) < 0 ? fromId : toId;
        UUID secondId = fromId.compareTo(toId) < 0 ? toId : fromId;

        Account first = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + firstId));

        Account second = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found: " + secondId));

        Account source = fromId.equals(firstId) ? first : second;
        Account destination = toId.equals(firstId) ? first : second;

        if (!source.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You are not authorized to debit this account");
        }

        if (source.getStatus() != AccountStatus.ACTIVE
                || destination.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Both accounts must be active");
        }

        if (!source.getCurrency().equals(destination.getCurrency())) {
            throw new IllegalArgumentException(
                    "Accounts must use the same currency");
        }

        if (source.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }
        riskAssessmentService.assessTransfer(
        userId,
        source.getId(),
        amount
);
        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));

        FinancialTransaction transaction = new FinancialTransaction();
        transaction.setFromAccountId(fromId);
        transaction.setToAccountId(toId);
        transaction.setAmount(amount);
        transaction.setCurrency(source.getCurrency());
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription(request.description());
        transaction.setIdempotencyKey(idempotencyKey);

        transaction = transactionRepository.saveAndFlush(transaction);

        LedgerEntry debit = new LedgerEntry();
        debit.setTransactionId(transaction.getId());
        debit.setAccountId(fromId);
        debit.setEntryType(LedgerEntryType.DEBIT);
        debit.setAmount(amount);
        debit.setCurrency(source.getCurrency());

        LedgerEntry credit = new LedgerEntry();
        credit.setTransactionId(transaction.getId());
        credit.setAccountId(toId);
        credit.setEntryType(LedgerEntryType.CREDIT);
        credit.setAmount(amount);
        credit.setCurrency(source.getCurrency());

        ledgerEntryRepository.save(debit);
        ledgerEntryRepository.save(credit);
        eventPublisher.publishEvent(
                new TransferCompletedEvent(
                        userId,
                        destination.getOwnerUserId(),
                        request.amount(),
                        source.getCurrency(),
                        transaction.getId().toString()
                )
        );

        return toResponse(transaction);
    }

    private TransferResponse toResponse(FinancialTransaction transaction) {
        return new TransferResponse(
                transaction.getId(),
                transaction.getFromAccountId(),
                transaction.getToAccountId(),
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getTransactionType(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt()
        );
    }
}
