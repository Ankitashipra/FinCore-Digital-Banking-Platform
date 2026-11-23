package com.fincore.account.service;

import com.fincore.account.entity.ScheduledTransfer;
import com.fincore.account.entity.ScheduledTransferStatus;
import com.fincore.account.repository.ScheduledTransferRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ScheduledTransferWorker {

    private final ScheduledTransferRepository repository;

    public ScheduledTransferWorker(ScheduledTransferRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ScheduledTransfer claim(UUID id) {

        ScheduledTransfer scheduled = repository.findByIdForUpdate(id)
                .orElse(null);

        if (scheduled == null
                || scheduled.getStatus() != ScheduledTransferStatus.SCHEDULED
                || scheduled.getScheduledAt().isAfter(
                        java.time.LocalDateTime.now())) {
            return null;
        }

        scheduled.setStatus(ScheduledTransferStatus.PROCESSING);

        return repository.save(scheduled);
    }

    @Transactional
    public void markCompleted(UUID id) {
        updateStatus(id, ScheduledTransferStatus.COMPLETED);
    }

    @Transactional
    public void markFailed(UUID id) {
        updateStatus(id, ScheduledTransferStatus.FAILED);
    }

    private void updateStatus(UUID id, ScheduledTransferStatus status) {

        ScheduledTransfer scheduled = repository.findByIdForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Scheduled transfer not found: " + id));

        if (scheduled.getStatus() == ScheduledTransferStatus.PROCESSING) {
            scheduled.setStatus(status);
            repository.save(scheduled);
        }
    }
}