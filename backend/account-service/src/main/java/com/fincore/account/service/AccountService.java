package com.fincore.account.service;

import com.fincore.account.dto.AccountResponse;
import com.fincore.account.dto.CreateAccountRequest;
import com.fincore.account.dto.AccountLookupResponse;
import com.fincore.account.entity.Account;
import com.fincore.account.entity.AccountStatus;
import com.fincore.account.exception.ResourceNotFoundException;
import com.fincore.account.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse createAccount(
            String ownerUserId,
            CreateAccountRequest request
    ) {
        UUID ownerId = UUID.fromString(ownerUserId);
        Account account = new Account();

        account.setOwnerUserId(ownerId);
        account.setAccountType(request.accountType());
        account.setAccountNumber(generateAccountNumber());
        account.setCurrency("INR");
        account.setStatus(AccountStatus.PENDING_APPROVAL);

        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getPendingAccounts() {
        return accountRepository.findByStatus(AccountStatus.PENDING_APPROVAL)
                .stream().map(AccountResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public AccountResponse decideAccount(UUID accountId, boolean approve) {
        Account account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account request not found"));
        if (account.getStatus() != AccountStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Only pending account requests can be reviewed");
        }
        account.setStatus(approve ? AccountStatus.ACTIVE : AccountStatus.REJECTED);
        return AccountResponse.from(accountRepository.save(account));
    }

    @Transactional(readOnly = true)
    public AccountLookupResponse lookupByAccountNumber(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Recipient account not found"));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Recipient account is not active");
        }
        return AccountLookupResponse.from(account);
    }

    private String generateAccountNumber() {
        String accountNumber;

        do {
            long number = ThreadLocalRandom.current()
                    .nextLong(1_000_000_000L, 10_000_000_000L);

            accountNumber = String.valueOf(number);

        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(String ownerUserId) {

    UUID userId = UUID.fromString(ownerUserId);

    return accountRepository.findByOwnerUserId(userId)
            .stream()
            .map(AccountResponse::from)
            .collect(Collectors.toList());
}
  @Transactional(readOnly = true)
public AccountResponse getAccountById(String ownerUserId, UUID accountId) {

    UUID userId = UUID.fromString(ownerUserId);

    Account account = accountRepository
            .findByIdAndOwnerUserId(accountId, userId)
            .orElseThrow(() -> new ResourceNotFoundException(
        "Account not found with ID: " + accountId
));

    return AccountResponse.from(account);
}
}