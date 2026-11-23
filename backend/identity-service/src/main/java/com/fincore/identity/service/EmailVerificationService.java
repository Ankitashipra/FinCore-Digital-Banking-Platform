package com.fincore.identity.service;

import com.fincore.identity.entity.EmailVerificationOtp;
import com.fincore.identity.entity.User;
import com.fincore.identity.repository.EmailVerificationOtpRepository;
import com.fincore.identity.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class EmailVerificationService {
    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private final EmailVerificationOtpRepository otpRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final ObjectProvider<JavaMailSender> mailSender;
    private final boolean devLogOtp;
    private final String fromAddress;
    private final String mailHost;
    private final SecureRandom random = new SecureRandom();

    public EmailVerificationService(EmailVerificationOtpRepository otpRepository,
            UserRepository userRepository, PasswordEncoder encoder,
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${app.mail.dev-log-otp:false}") boolean devLogOtp,
            @Value("${app.mail.from:no-reply@fincore.local}") String fromAddress,
            @Value("${spring.mail.host:}") String mailHost) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.mailSender = mailSender;
        this.devLogOtp = devLogOtp;
        this.fromAddress = fromAddress;
        this.mailHost = mailHost;
    }

    @Transactional
    public void send(User user) {
        EmailVerificationOtp previous = otpRepository.findByUserId(user.getId()).orElse(null);
        if (previous != null && previous.getLastSentAt().isAfter(LocalDateTime.now().minusSeconds(60))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Please wait 60 seconds before requesting another OTP");
        }
        String otp = String.format("%06d", random.nextInt(1_000_000));
        EmailVerificationOtp record = previous == null ? new EmailVerificationOtp() : previous;
        record.setUserId(user.getId());
        record.setOtpHash(encoder.encode(otp));
        record.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        record.setAttempts(0);
        record.setLastSentAt(LocalDateTime.now());
        otpRepository.save(record);

        JavaMailSender sender = mailSender.getIfAvailable();
        if (sender != null && !mailHost.isBlank()) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(user.getEmail());
            message.setSubject("Your FinCore verification code");
            message.setText("Hello " + user.getFullName() + ",\n\nYour FinCore verification code is " + otp +
                    ". It expires in 5 minutes. Do not share this code with anyone.");
            sender.send(message);
        } else if (devLogOtp) {
            log.warn("DEVELOPMENT ONLY: FinCore OTP for {} is {}", user.getEmail(), otp);
        } else {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Email delivery is not configured. Contact the administrator.");
        }
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void verify(String email, String otp) {
        User user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registration not found"));
        if (user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already verified");
        }
        EmailVerificationOtp record = otpRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request a verification code first"));
        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "OTP expired. Request a new code.");
        }
        if (record.getAttempts() >= 5) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many incorrect attempts. Request a new code.");
        }
        record.setAttempts(record.getAttempts() + 1);
        if (!encoder.matches(otp, record.getOtpHash())) {
            otpRepository.save(record);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification code");
        }
        user.setEmailVerified(true);
        user.setActive(false);
        user.setApplicationStatus("PENDING_REVIEW");
        otpRepository.deleteByUserId(user.getId());
    }

}
