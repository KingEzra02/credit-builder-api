package com.creditbuilder.api.service;

import com.creditbuilder.api.entity.CreditScore;
import com.creditbuilder.api.entity.Transaction;
import com.creditbuilder.api.entity.User;
import com.creditbuilder.api.repository.CreditScoreRepository;
import com.creditbuilder.api.repository.TransactionRepository;
import com.creditbuilder.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CreditScoringService {
    private final TransactionRepository transactionRepository;
    private final CreditScoreRepository creditScoreRepository;
    private final UserRepository userRepository;

    // Base score everyone start with
    private static final int BASE_SCORE = 100;
    private static final int MAX_SAVINGS_SCORE = 200;
    private static final int MAX_CONSISTENCY_SCORE = 200;
    private static final int MAX_STREAK_SCORE = 200;
    private static final int MAX_INCOME_SCORE = 150;

    /**
     * Main method: calculate, saves and returns a new credit score
     * Called after every transaction is logged
     */

    public CreditScore calculateAndSave(UUID userId){
        User user  = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get last 90 days of transaction for scoring
        LocalDate since = LocalDate.now().minusDays(90);
        List<Transaction> recentTransaction =
                transactionRepository.findByUserIdSince(userId, since);

        // Calculate each factor

        Map<String, Integer> breakdown = new LinkedHashMap<>();
        breakdown.put("baseScore", BASE_SCORE);
        breakdown.put("savingsScore", calculateSavingsScore(userId));
        breakdown.put("consistencyScore", calculateConsistencyScore(recentTransaction));
        breakdown.put("streakScore", calculateStreakScore(userId));
        breakdown.put("incomeScore", calculateIncomeScore(recentTransaction));


        // Sum all factors
        int totalScore = breakdown.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        // Clamp between 300 and 850
        totalScore = Math.max(300, Math.min(850, totalScore));

        // Build and save the score record
        CreditScore creditScore = CreditScore.builder()
                .user(user)
                .score(totalScore)
                .level(CreditScore.calculateLevel(totalScore))
                .scoreBreakdown(breakdown)
                .build();

        return creditScoreRepository.save(creditScore);
    }

    /**
     * FACTOR 1: Savings Score (max 200 pts)
     * Rewards users who save a portion of their income
     * Saving 20%+ oif income = full points
     */

    private int calculateSavingsScore(UUID userId){
        BigDecimal totalIncome = transactionRepository
                .sumByUserIdAndType(userId, Transaction.TransactionType.INCOME);

        BigDecimal totalExpenses = transactionRepository
                .sumByUserIdAndType(userId, Transaction.TransactionType.EXPENSE);

        // No income → no savings score
        if (totalIncome == null || totalIncome.compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }

        BigDecimal savings = totalIncome.subtract(totalExpenses);

        BigDecimal savingsRate = savings.divide(totalIncome, 4, RoundingMode.HALF_UP);

        double rate = savingsRate.doubleValue();

        if (rate >= 0.20) return MAX_SAVINGS_SCORE;
        if (rate <= 0) return 0;

        return (int) (rate / 0.20 * MAX_SAVINGS_SCORE);
    }

    /**
     * FACTOR 2: Spending Consistency Score (max 200 pts)
     * Rewards users who spend in predictable, stable patterns
     * More categories used = more stable lifestyle = higher score
     */

    private int calculateConsistencyScore(List<Transaction> transactions){
        if(transactions.isEmpty()) return 0;

        long expenseCount = transactions.stream()
                .filter(t  -> t.getType() == Transaction.TransactionType.EXPENSE)
                .count();

        if(expenseCount == 0) return 0;

        // Count distinct spending categories
        long distinctCategories = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE )
                .map(Transaction::getCategory)
                .distinct()
                .count();

        // 5+ categories = consistent, stable spender
        double ratio = Math.min(distinctCategories / 5.0, 1.0);
        return (int) (ratio * MAX_CONSISTENCY_SCORE);

    }

    /**
     * FACTOR 3: streak score (max 200 pts)
     * Rewards users who consistently log transactions
     * Logging every week for 12+ weeks = full points
     */

    private int calculateStreakScore(UUID userId){
        Long weeksActive = transactionRepository
                .countDistinctWeeksByUserId(userId);

        if(weeksActive == null || weeksActive == 0) return 0;

        // 12 weeks of consistency logging = full score
        double ratio = Math.min(weeksActive / 12.0, 1.0);
        return (int) (ratio * MAX_STREAK_SCORE);
    }

    /**
     * FACTOR 4: Income Regularity Score (max 150 pts)
     * Rewards users with consistent, regular income
     * Having income in 3 of last 3 months = full points
     */

    private int calculateIncomeScore(List<Transaction> transactions){
        long monthsWithIncome = transactions.stream()
                .filter(t ->t.getType() == Transaction.TransactionType.INCOME)
                .map(t -> t.getDate().getMonth())
                .distinct()
                .count();

        if(monthsWithIncome == 0) return 0;

        // Income in 3 months == full score
        double ratio = Math.min(monthsWithIncome / 3.0, 1.0);
        return (int) (ratio * MAX_INCOME_SCORE);


    }

    /**
     * Get current score for a user (most recent)
     */
    public Optional<CreditScore> getCurrentScore(UUID userId){
        return creditScoreRepository
                .findTopByUserIdOrderByCalculatedAtDesc(userId);
    }
    /**
     *  Get full score history for a user
     */

    public List<CreditScore> getScoreHistory(UUID userId){
        return  creditScoreRepository
                .findByUserIdOrderByCalculatedAtAsc(userId);
    }

}
