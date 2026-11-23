package com.fincore.identity.controller;

import com.fincore.identity.dto.AdminCustomerDecision;
import com.fincore.identity.dto.AdminCustomerView;
import com.fincore.identity.entity.User;
import com.fincore.identity.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/customers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCustomerController {
    private final UserRepository users;
    public AdminCustomerController(UserRepository users) { this.users = users; }

    @GetMapping
    public List<AdminCustomerView> list() {
        return users.findAll().stream().filter(u -> "CUSTOMER".equals(u.getRole()))
                .map(this::view).toList();
    }

    @PutMapping("/{id}/decision")
    @Transactional
    public AdminCustomerView decide(@PathVariable String id,
            @Valid @RequestBody AdminCustomerDecision request, Authentication authentication) {
        User target = users.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
        if (!"CUSTOMER".equals(target.getRole())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only customer applications can be reviewed");
        if (target.getEmail().equalsIgnoreCase(authentication.getName())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "An administrator cannot review their own application");
        if (!target.isEmailVerified()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer must verify email before review");
        String decision = request.decision().trim().toUpperCase();
        if (!decision.equals("APPROVE") && !decision.equals("REJECT")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision must be APPROVE or REJECT");
        if (!target.getApplicationStatus().equals("PENDING_REVIEW")) throw new ResponseStatusException(HttpStatus.CONFLICT, "Application has already been reviewed");
        target.setApplicationStatus(decision.equals("APPROVE") ? "APPROVED" : "REJECTED");
        target.setActive(decision.equals("APPROVE"));
        target.setReviewedBy(authentication.getName());
        target.setReviewedAt(LocalDateTime.now());
        target.setReviewReason(request.reason() == null ? null : request.reason().trim());
        return view(users.save(target));
    }

    private AdminCustomerView view(User u) {
        return new AdminCustomerView(u.getId(), u.getFullName(), u.getEmail(), u.getApplicationStatus(),
                u.isEmailVerified(), u.getCreatedAt(), u.getReviewedBy(), u.getReviewedAt(), u.getReviewReason());
    }
}
