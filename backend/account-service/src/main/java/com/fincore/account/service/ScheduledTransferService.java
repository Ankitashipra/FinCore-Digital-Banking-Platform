package com.fincore.account.service;

import com.fincore.account.dto.ScheduledTransferRequest;
import com.fincore.account.dto.ScheduledTransferResponse;
import com.fincore.account.entity.*;
import com.fincore.account.repository.AccountRepository;
import com.fincore.account.repository.ScheduledTransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ScheduledTransferService {

    private final AccountRepository accountRepository;
    private final ScheduledTransferRepository scheduledRepository;

    public ScheduledTransferService(
            AccountRepository accountRepository,
            ScheduledTransferRepository scheduledRepository) {
        this.accountRepository = accountRepository;
        this.scheduledRepository = scheduledRepository;
    }

    @Transactional
    public ScheduledTransferResponse create(
            String ownerUserId,
            ScheduledTransferRequest request) {

        UUID userId = UUID.fromString(ownerUserId);

        if (request.fromAccountId().equals(request.toAccountId())) {
            throw new IllegalArgumentException(
                    "Source and destination accounts must be different");
        }

        if (!request.scheduledAt().isAfter(
                java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Scheduled time must be in the future");
        }

        if (request.amount().scale() > 2
                || request.amount().precision() > 19) {
            throw new IllegalArgumentException("Invalid transfer amount");
        }

        Account source = accountRepository
                .findByIdAndOwnerUserId(request.fromAccountId(), userId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Source account not found"));

        Account destination = accountRepository
                .findById(request.toAccountId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Destination account not found"));

        if (source.getStatus() != AccountStatus.ACTIVE
                || destination.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Both accounts must be active");
        }

        if (!source.getCurrency().equals(destination.getCurrency())) {
            throw new IllegalArgumentException("Account currencies must match");
        }

        ScheduledTransfer transfer = new ScheduledTransfer();
        transfer.setOwnerUserId(userId);
        transfer.setFromAccountId(source.getId());
        transfer.setToAccountId(destination.getId());
        transfer.setAmount(request.amount());
        transfer.setCurrency(source.getCurrency());
        transfer.setDescription(request.description());
        transfer.setScheduledAt(request.scheduledAt());
        transfer.setStatus(ScheduledTransferStatus.SCHEDULED);

        return ScheduledTransferResponse.from(
                scheduledRepository.save(transfer));
    }

    @Transactional(readOnly = true)
    public List<ScheduledTransferResponse> getMyTransfers(
            String ownerUserId) {

        UUID userId = UUID.fromString(ownerUserId);

        return scheduledRepository
                .findByOwnerUserIdOrderByScheduledAtAsc(userId)
                .stream()
                .map(ScheduledTransferResponse::from)
                .toList();
    }

    @Transactional
    public void cancel(String ownerUserId, UUID transferId) {

        UUID userId = UUID.fromString(ownerUserId);

        ScheduledTransfer transfer = scheduledRepository.findById(transferId)
                .filter(item -> item.getOwnerUserId().equals(userId))
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Scheduled transfer not found"));

        if (transfer.getStatus() != ScheduledTransferStatus.SCHEDULED) {
            throw new IllegalArgumentException(
                    "Only scheduled transfers can be cancelled");
        }

        transfer.setStatus(ScheduledTransferStatus.CANCELLED);
    }
}