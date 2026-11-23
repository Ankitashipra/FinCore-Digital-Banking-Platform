package com.fincore.account.service;

import com.fincore.account.dto.BeneficiaryRequest;
import com.fincore.account.dto.BeneficiaryResponse;
import com.fincore.account.entity.Account;
import com.fincore.account.entity.AccountStatus;
import com.fincore.account.entity.Beneficiary;
import com.fincore.account.repository.AccountRepository;
import com.fincore.account.repository.BeneficiaryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final AccountRepository accountRepository;

    public BeneficiaryService(
            BeneficiaryRepository beneficiaryRepository,
            AccountRepository accountRepository) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public BeneficiaryResponse addBeneficiary(
            String ownerId, BeneficiaryRequest request) {

        UUID userId = UUID.fromString(ownerId);

        String accountNumber = request.accountNumber().trim();

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Beneficiary account was not found"));

        if (account.getOwnerUserId().equals(userId)) {
            throw new IllegalArgumentException(
                    "You cannot add your own account as a beneficiary");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Beneficiary account is not active");
        }

        if (beneficiaryRepository
                .existsByOwnerUserIdAndBeneficiaryAccountId(
                        userId, account.getId())) {
            throw new IllegalArgumentException(
                    "This beneficiary has already been added");
        }

        Beneficiary beneficiary = new Beneficiary();
        beneficiary.setOwnerUserId(userId);
        beneficiary.setBeneficiaryAccountId(account.getId());
        beneficiary.setAccountNumber(account.getAccountNumber());
        beneficiary.setNickname(request.nickname().trim());

        return BeneficiaryResponse.from(
                beneficiaryRepository.save(beneficiary));
    }

    @Transactional(readOnly = true)
    public List<BeneficiaryResponse> getMyBeneficiaries(String ownerId) {

        UUID userId = UUID.fromString(ownerId);

        return beneficiaryRepository
                .findByOwnerUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(BeneficiaryResponse::from)
                .toList();
    }

    @Transactional
    public void deleteBeneficiary(String ownerId, UUID beneficiaryId) {

        UUID userId = UUID.fromString(ownerId);

        Beneficiary beneficiary = beneficiaryRepository
                .findByIdAndOwnerUserId(beneficiaryId, userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Beneficiary not found"));

        beneficiaryRepository.delete(beneficiary);
    }
}