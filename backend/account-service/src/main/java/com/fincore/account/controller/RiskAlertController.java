package com.fincore.account.controller;

import com.fincore.account.dto.RiskAlertResponse;
import com.fincore.account.service.RiskAlertService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/risk-alerts")
public class RiskAlertController {

    private final RiskAlertService riskAlertService;

    public RiskAlertController(RiskAlertService riskAlertService) {
        this.riskAlertService = riskAlertService;
    }

    @GetMapping
    public List<RiskAlertResponse> getMyAlerts(
            Authentication authentication) {
        return riskAlertService.getMyAlerts(authentication.getName());
    }

    @PatchMapping("/{alertId}/review")
    public RiskAlertResponse markReviewed(
            @PathVariable UUID alertId,
            Authentication authentication) {
        return riskAlertService.markReviewed(
                authentication.getName(), alertId);
    }
}