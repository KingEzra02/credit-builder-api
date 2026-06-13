package com.creditbuilder.api.controller;

import com.creditbuilder.api.entity.LoanApplication;
import com.creditbuilder.api.repository.UserRepository;
import com.creditbuilder.api.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final UserRepository userRepository;

    @PostMapping("/apply")
    public ResponseEntity<LoanApplication> apply(
            @AuthenticationPrincipal UserDetails userDetails
            ){
        UUID userId = getUserId(userDetails);
        return ResponseEntity.ok(loanService.apply(userId));
    }

    @GetMapping("/my-applications")
    public ResponseEntity<List<LoanApplication>> myApplication(
            @AuthenticationPrincipal UserDetails userDetails) {
        UUID userId = getUserId(userDetails);
        return ResponseEntity.ok(loanService.getMyApplicationsI(userId));
    }

    private UUID getUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found")).getId();
    }
}
