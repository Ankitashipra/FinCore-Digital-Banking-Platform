package com.fincore.account.controller;

import com.fincore.account.dto.ScheduledTransferRequest;
import com.fincore.account.dto.ScheduledTransferResponse;
import com.fincore.account.service.ScheduledTransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/scheduled-transfers")
public class ScheduledTransferController {

    private final ScheduledTransferService service;

    public ScheduledTransferController(
            ScheduledTransferService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ScheduledTransferResponse create(
            @Valid @RequestBody ScheduledTransferRequest request,
            Authentication authentication) {

        return service.create(authentication.getName(), request);
    }

    @GetMapping
    public List<ScheduledTransferResponse> getMyTransfers(
            Authentication authentication) {

        return service.getMyTransfers(authentication.getName());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @PathVariable UUID id,
            Authentication authentication) {

        service.cancel(authentication.getName(), id);
    }
}