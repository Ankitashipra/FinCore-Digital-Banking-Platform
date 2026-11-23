package com.fincore.identity.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_otps", indexes = @Index(name = "idx_email_otp_user", columnList = "user_id"))
public class EmailVerificationOtp {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;
    @Column(name = "otp_hash", nullable = false, length = 100)
    private String otpHash;
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    @Column(nullable = false)
    private int attempts;
    @Column(name = "last_sent_at", nullable = false)
    private LocalDateTime lastSentAt;

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public void setUserId(String v) { userId = v; }
    public String getOtpHash() { return otpHash; }
    public void setOtpHash(String v) { otpHash = v; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime v) { expiresAt = v; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int v) { attempts = v; }
    public LocalDateTime getLastSentAt() { return lastSentAt; }
    public void setLastSentAt(LocalDateTime v) { lastSentAt = v; }
}
