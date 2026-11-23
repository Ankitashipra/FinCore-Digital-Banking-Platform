package com.fincore.account.controller;

import com.fincore.account.dto.BeneficiaryRequest;
import com.fincore.account.dto.BeneficiaryResponse;
import com.fincore.account.service.BeneficiaryService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BeneficiaryResponse addBeneficiary(
            @Valid @RequestBody BeneficiaryRequest request,
            Authentication authentication) {

        return beneficiaryService.addBeneficiary(
                authentication.getName(), request);
    }

    @GetMapping
    public List<BeneficiaryResponse> getMyBeneficiaries(
            Authentication authentication) {

        return beneficiaryService.getMyBeneficiaries(
                authentication.getName());
    }

    @DeleteMapping("/{beneficiaryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBeneficiary(
            @PathVariable UUID beneficiaryId,
            Authentication authentication) {

        beneficiaryService.deleteBeneficiary(
                authentication.getName(), beneficiaryId);
    }
}