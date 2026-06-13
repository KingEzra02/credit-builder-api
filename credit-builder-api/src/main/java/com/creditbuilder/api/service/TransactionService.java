package com.creditbuilder.api.service;

import com.creditbuilder.api.dto.TransactionRequest;
import com.creditbuilder.api.dto.TransactionResponse;
import com.creditbuilder.api.entity.CreditScore;
import com.creditbuilder.api.entity.Transaction;
import com.creditbuilder.api.entity.User;
import com.creditbuilder.api.repository.TransactionRepository;
import com.creditbuilder.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final CreditScoringService creditScoringService;
    private final RewardService rewardService;

    public TransactionResponse logTransaction(UUID userId, TransactionRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User is not found"));

        Transaction transaction = Transaction.builder()
                .user(user)
                .type(request.getType())
                .amount(request.getAmount())
                .category(request.getCategory())
                .description(request.getDescription())
                .date(request.getDate())
                .build();

        Transaction saved  = transactionRepository.save(transaction);

        // Recalculate score
        CreditScore newScore = creditScoringService.calculateAndSave(userId);

        // Check and award badges based on new score
        rewardService.checkAndAwardBadges(userId, newScore.getScore());
        // This is what makes the score "behavioral" and real-time

        creditScoringService.calculateAndSave(userId);

        return TransactionResponse.from(saved);
    }

    public List<TransactionResponse> getUserTransactions(UUID userId){
        return transactionRepository
                .findByUserIdOrderByDateDesc(userId)
                .stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }
}
