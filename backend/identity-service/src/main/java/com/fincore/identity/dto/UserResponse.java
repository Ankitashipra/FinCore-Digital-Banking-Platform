
package com.fincore.identity.dto;

import java.time.LocalDateTime;

public record UserResponse(
    String id,
    String fullName,
    String email,
    String role,
    LocalDateTime createdAt
) {}
