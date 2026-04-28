package com.creditbuilder.api.controller;

import com.creditbuilder.api.dto.TransactionRequest;
import com.creditbuilder.api.dto.TransactionResponse;
import com.creditbuilder.api.entity.CreditScore;
import com.creditbuilder.api.entity.User;
import com.creditbuilder.api.repository.UserRepository;
import com.creditbuilder.api.service.CreditScoringService;
import com.creditbuilder.api.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;
    private final CreditScoringService creditScoringService;
    private final UserRepository userRepository;

    @SuppressWarnings("CallToPrintStackTrace")
    @PostMapping
    public ResponseEntity<TransactionResponse> logTransaction(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionRequest request
            ){
        UUID userId = getUserId(userDetails);
    try {
        return ResponseEntity.ok(transactionService.logTransaction(userId, request));
    } catch (Exception e){
        e.printStackTrace();
        throw  e;
    }
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getMyTransactions(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        UUID userId = getUserId(userDetails);
        return ResponseEntity.ok    (transactionService.getUserTransactions(userId));
    }

    @GetMapping("/score")
    public ResponseEntity<?> getMyScore(
            @AuthenticationPrincipal UserDetails userDetails
    ){
        UUID userId = getUserId(userDetails);
        Optional<CreditScore> score = creditScoringService.getCurrentScore(userId);

        return score.map(s -> ResponseEntity.ok(Map.of(
                "score", s.getScore(),
                "level", s.getLevel(),
                "breakdown", s.getScoreBreakdown(),
                "calculatedAt", s.getCalculatedAt()
        ))).orElse(ResponseEntity.ok(Map.of(
                "message", "No score yet — log your first transaction!"
        )));
    }

    // Helper: extract UUID from the logged-in user's JWT
    private UUID getUserId(UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getId();
    }
}
