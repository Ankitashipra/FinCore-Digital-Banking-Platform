package com.fincore.account.dto;

import com.fincore.account.entity.Beneficiary;
import java.time.LocalDateTime;
import java.util.UUID;

public record BeneficiaryResponse(
        UUID id,
        UUID beneficiaryAccountId,
        String accountNumber,
        String nickname,
        LocalDateTime createdAt
) {
    public static BeneficiaryResponse from(Beneficiary beneficiary) {
        return new BeneficiaryResponse(
                beneficiary.getId(),
                beneficiary.getBeneficiaryAccountId(),
                beneficiary.getAccountNumber(),
                beneficiary.getNickname(),
                beneficiary.getCreatedAt()
        );
    }
}