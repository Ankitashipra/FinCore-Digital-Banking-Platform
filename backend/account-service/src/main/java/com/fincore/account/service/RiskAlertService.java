package com.fincore.account.service;

import com.fincore.account.dto.RiskAlertResponse;
import com.fincore.account.entity.RiskAlert;
import com.fincore.account.entity.RiskAlertStatus;
import com.fincore.account.repository.RiskAlertRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class RiskAlertService {

    private final RiskAlertRepository repository;

    public RiskAlertService(RiskAlertRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<RiskAlertResponse> getMyAlerts(String ownerId) {
        return repository.findByOwnerUserIdOrderByCreatedAtDesc(
                        UUID.fromString(ownerId))
                .stream()
                .map(RiskAlertResponse::from)
                .toList();
    }

    @Transactional
    public RiskAlertResponse markReviewed(
            String ownerId, UUID alertId) {

        RiskAlert alert = repository.findByIdAndOwnerUserId(
                        alertId, UUID.fromString(ownerId))
                .orElseThrow(() ->
                        new IllegalArgumentException("Risk alert not found"));

        alert.setStatus(RiskAlertStatus.REVIEWED);

        return RiskAlertResponse.from(repository.save(alert));
    }
}