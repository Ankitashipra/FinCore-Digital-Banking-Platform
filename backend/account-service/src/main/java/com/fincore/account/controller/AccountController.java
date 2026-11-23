package com.fincore.account.controller;

import com.fincore.account.dto.AccountResponse;
import com.fincore.account.dto.AccountLookupResponse;
import com.fincore.account.dto.CreateAccountRequest;
import com.fincore.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AccountResponse>> getPendingAccountRequests() {
        return ResponseEntity.ok(accountService.getPendingAccounts());
    }

    @PutMapping("/admin/{accountId}/decision")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> decideAccountRequest(
            @PathVariable UUID accountId, @RequestBody AccountDecisionRequest request) {
        boolean approve = "APPROVE".equalsIgnoreCase(request.decision());
        if (!approve && !"REJECT".equalsIgnoreCase(request.decision())) {
            throw new IllegalArgumentException("Decision must be APPROVE or REJECT");
        }
        return ResponseEntity.ok(accountService.decideAccount(accountId, approve));
    }

    public record AccountDecisionRequest(String decision) {}

    @GetMapping("/lookup/{accountNumber}")
    public ResponseEntity<AccountLookupResponse> lookupRecipient(@PathVariable String accountNumber) {
        return ResponseEntity.ok(accountService.lookupByAccountNumber(accountNumber));
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request,
            Authentication authentication
    ) {
        AccountResponse response = accountService.createAccount(
                authentication.getName(),
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
public ResponseEntity<List<AccountResponse>> getMyAccounts(
        Authentication authentication
) {
    List<AccountResponse> accounts = accountService.getMyAccounts(
            authentication.getName()
    );

    return ResponseEntity.ok(accounts);
}
@GetMapping("/{accountId}")
public ResponseEntity<AccountResponse> getAccountById(
        @PathVariable UUID accountId,
        Authentication authentication
) {
    AccountResponse account = accountService.getAccountById(
            authentication.getName(),
            accountId
    );

    return ResponseEntity.ok(account);
}
}