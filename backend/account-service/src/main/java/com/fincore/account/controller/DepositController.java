package com.fincore.account.controller;

import com.fincore.account.dto.DepositRequest;
import com.fincore.account.dto.DepositResponse;
import com.fincore.account.service.DepositService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class DepositController {

    private final DepositService depositService;

    public DepositController(DepositService depositService) {
        this.depositService = depositService;
    }

    @PostMapping("/{accountId}/demo-deposits")
    @ResponseStatus(HttpStatus.CREATED)
    public DepositResponse deposit(
            @PathVariable UUID accountId,
            @Valid @RequestBody DepositRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {

        return depositService.deposit(
                authentication.getName(),
                accountId,
                request,
                idempotencyKey
        );
    }
}