package com.creditbuilder.api.service;

import com.creditbuilder.api.entity.CreditScore;
import com.creditbuilder.api.entity.LoanApplication;
import com.creditbuilder.api.entity.User;
import com.creditbuilder.api.repository.CreditScoreRepository;
import com.creditbuilder.api.repository.LoanApplicationRepository;
import com.creditbuilder.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanApplicationRepository loanRepo;
    private final CreditScoreRepository creditScoreRepo;
    private final UserRepository userRepository;

    public LoanApplication apply(UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User not found"));

        CreditScore latestScore = creditScoreRepo
                .findTopByUserIdOrderByCalculatedAtDesc(userId)
                .orElseThrow(()-> new RuntimeException("No credit score yet. Log transaction first"));

        int score = latestScore.getScore();

        BigDecimal amount;
        LoanApplication.LoanStatus status;
        String reason;

        if(score >= 751){
            amount = BigDecimal.valueOf(500);
            status = LoanApplication.LoanStatus.APPROVED;
            reason = "Excellent behavioral score, Maximum credit granted";
        } else if (score >= 651){
            amount =  BigDecimal.valueOf(350);
            status = LoanApplication.LoanStatus.APPROVED;
            reason = "Fair behavioral score. Entry-level credit granted";
        } else {
            amount = BigDecimal.ZERO;
            status = LoanApplication.LoanStatus.REJECTED;
            reason = "Score below 500. Continue logging transactions to improve.";
        }

        LoanApplication application = LoanApplication.builder()
                .user(user)
                .amountRequested(amount)
                .status(status)
                .decisionReason(reason)
                .creditScoreAtApplication(score)
                .build();

        return loanRepo.save(application);
    }

    public List<LoanApplication> getMyApplicationsI(UUID userId){
        return loanRepo.findByUserIdOrderByAppliedAtDesc(userId);
    }
}
