package com.fincore.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCustomerDecision(
        @NotBlank String decision,
        @Size(max = 500) String reason) { }
