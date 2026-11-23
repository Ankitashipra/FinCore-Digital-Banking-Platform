
package com.fincore.account.service;

import com.fincore.account.dto.TransactionResponse;
import com.fincore.account.entity.*;
import com.fincore.account.repository.AccountRepository;
import com.fincore.account.repository.FinancialTransactionRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionHistoryService {

    private final AccountRepository accountRepository;
    private final FinancialTransactionRepository transactionRepository;

    public TransactionHistoryService(
            AccountRepository accountRepository,
            FinancialTransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getMyTransactions(
            String ownerUserId,
            int page,
            int size,
            TransactionType type,
            TransactionStatus status) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Invalid pagination parameters");
        }

        UUID userId = UUID.fromString(ownerUserId);

        List<UUID> accountIds = accountRepository.findByOwnerUserId(userId)
                .stream()
                .map(Account::getId)
                .toList();

        if (accountIds.isEmpty()) {
            return Page.empty();
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<FinancialTransaction> transactions =
                transactionRepository.findUserTransactions(
                        accountIds, type, status, pageable);

        return transactions.map(this::toResponse);
    }

    private TransactionResponse toResponse(
            FinancialTransaction transaction) {

        return new TransactionResponse(
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
