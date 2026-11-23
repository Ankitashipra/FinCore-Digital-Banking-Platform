package com.fincore.account.controller;

import com.fincore.account.dto.TransactionResponse;
import com.fincore.account.entity.TransactionStatus;
import com.fincore.account.entity.TransactionType;
import com.fincore.account.service.TransactionHistoryService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionHistoryController {

    private final TransactionHistoryService historyService;

    public TransactionHistoryController(TransactionHistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public Page<TransactionResponse> getMyTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) TransactionStatus status) {

        return historyService.getMyTransactions(
                authentication.getName(), page, size, type, status);
    }
}