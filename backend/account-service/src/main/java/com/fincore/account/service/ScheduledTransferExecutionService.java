package com.fincore.account.service;

import com.fincore.account.dto.TransferRequest;
import com.fincore.account.entity.ScheduledTransfer;
import com.fincore.account.entity.ScheduledTransferStatus;
import com.fincore.account.repository.ScheduledTransferRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ScheduledTransferExecutionService {

    private static final Logger log =
            LoggerFactory.getLogger(ScheduledTransferExecutionService.class);

    private final ScheduledTransferRepository repository;
    private final ScheduledTransferWorker worker;
    private final TransferService transferService;

    public ScheduledTransferExecutionService(
            ScheduledTransferRepository repository,
            ScheduledTransferWorker worker,
            TransferService transferService) {

        this.repository = repository;
        this.worker = worker;
        this.transferService = transferService;
    }

    @Scheduled(fixedDelay = 60000)
    public void executeDueTransfers() {

        var dueIds = repository.findDueTransferIds(
                ScheduledTransferStatus.SCHEDULED,
                LocalDateTime.now());

        for (UUID id : dueIds) {
            processTransfer(id);
        }
    }

    private void processTransfer(UUID id) {

        ScheduledTransfer scheduled = worker.claim(id);

        if (scheduled == null) {
            return;
        }

        try {
            TransferRequest request = new TransferRequest(
                    scheduled.getFromAccountId(),
                    scheduled.getToAccountId(),
                    scheduled.getAmount(),
                    scheduled.getDescription()
            );

            transferService.transfer(
                    scheduled.getOwnerUserId().toString(),
                    request,
                    "scheduled-" + scheduled.getId()
            );

            worker.markCompleted(id);

            log.info("Scheduled transfer {} completed", id);

        } catch (Exception exception) {

            worker.markFailed(id);

            log.error(
                    "Scheduled transfer {} failed: {}",
                    id,
                    exception.getMessage()
            );
        }
    }
}