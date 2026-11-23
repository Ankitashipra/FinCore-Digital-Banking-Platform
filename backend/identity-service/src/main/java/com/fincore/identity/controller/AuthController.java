
package com.fincore.identity.controller;

import com.fincore.identity.dto.RegisterRequest;
import com.fincore.identity.dto.UserResponse;
import com.fincore.identity.dto.LoginRequest;
import com.fincore.identity.dto.AuthResponse;
import com.fincore.identity.dto.VerifyEmailRequest;
import com.fincore.identity.service.EmailVerificationService;
import com.fincore.identity.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import com.fincore.identity.service.UserService;
import com.fincore.identity.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final UserRepository userRepository;

    public AuthController(
            UserService userService,
            AuthService authService,
            EmailVerificationService emailVerificationService,
            UserRepository userRepository) {

        this.userService = userService;
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return userService.register(request);
    }


    @PostMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verify(request.email(), request.otp());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(@RequestParam String email) {
        var user = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Registration not found"));
        if (user.isEmailVerified()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.CONFLICT, "Email is already verified. Your application is awaiting review.");
        }
        emailVerificationService.send(user);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login")
    public AuthResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }
}
