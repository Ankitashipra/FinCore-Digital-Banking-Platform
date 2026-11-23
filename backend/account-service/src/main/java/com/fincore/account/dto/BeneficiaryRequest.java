package com.fincore.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BeneficiaryRequest(

        @NotBlank
        String accountNumber,

        @NotBlank
        @Size(min = 2, max = 80)
        String nickname
) {}