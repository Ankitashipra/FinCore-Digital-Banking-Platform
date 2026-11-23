package com.fincore.account.controller;

import com.fincore.account.dto.TransferRequest;
import com.fincore.account.dto.TransferResponse;
import com.fincore.account.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            Authentication authentication) {

        String ownerUserId = authentication.getName();

        return transferService.transfer(
                ownerUserId,
                request,
                idempotencyKey
        );
    }
}
