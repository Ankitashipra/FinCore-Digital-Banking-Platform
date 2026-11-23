package com.fincore.identity.repository;

import com.fincore.identity.entity.EmailVerificationOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailVerificationOtpRepository extends JpaRepository<EmailVerificationOtp, String> {
    Optional<EmailVerificationOtp> findByUserId(String userId);
    void deleteByUserId(String userId);
}
