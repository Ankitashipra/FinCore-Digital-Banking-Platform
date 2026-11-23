package com.fincore.account.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationClient notificationClient;

    public NotificationEventListener(NotificationClient notificationClient) {
        this.notificationClient = notificationClient;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTransferCompleted(TransferCompletedEvent event) {
        try {
            notificationClient.send(event.senderUserId(), "TRANSFER_SUCCESS",
                    "Transfer successful",
                    "Your transfer of " + event.amount() + " " + event.currency()
                            + " was sent. Reference: " + event.reference());
        } catch (Exception exception) {
            log.error("Could not send sender notification for transfer {}", event.reference(), exception);
        }
        if (!event.senderUserId().equals(event.recipientUserId())) {
            try {
                notificationClient.send(event.recipientUserId(), "TRANSFER_SUCCESS",
                        "Money received",
                        "You received " + event.amount() + " " + event.currency()
                                + ". Reference: " + event.reference());
            } catch (Exception exception) {
                log.error("Could not send recipient notification for transfer {}", event.reference(), exception);
            }
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRiskAlertCreated(RiskAlertCreatedEvent event) {
        try {
            notificationClient.send(
                    event.userId(),
                    "RISK_ALERT",
                    "Financial risk alert",
                    event.description()
            );
        } catch (Exception exception) {
            log.error("Could not send risk notification for user {}",
                    event.userId(), exception);
        }
    }
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onDepositCompleted(DepositCompletedEvent event) {
    try {
        notificationClient.send(
                event.userId(),
                "ACCOUNT_UPDATE",
                "Deposit successful",
                "Your account has been credited with "
                        + event.amount() + " " + event.currency() + "."
        );
    } catch (Exception exception) {
        log.error("Could not send deposit notification for user {}",
                event.userId(), exception);
    }
}
}