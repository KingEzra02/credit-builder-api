package com.creditbuilder.api.service;

import com.creditbuilder.api.entity.Reward;
import com.creditbuilder.api.entity.User;
import com.creditbuilder.api.repository.RewardRepository;
import com.creditbuilder.api.repository.TransactionRepository;
import com.creditbuilder.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RewardService {

    private final RewardRepository rewardRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Called after every transaction - check and marks and awards any new badges
     */

    public List<Reward> checkAndAwardBadges(UUID userId, int currentScore){
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("User not found"));

        List<Reward> newBadges = new ArrayList<>();

        // Badge 1: First Transaction
        long txCount = transactionRepository.countDistinctWeeksByUserId(userId);
        if(!rewardRepository.existsByUserIdAndBadgeType(userId, Reward.BadgeType.FIRST_TRANSACTION)){
            newBadges.add(awardBadge(user, "First Step", Reward.BadgeType.FIRST_TRANSACTION, 0));
        }

        // Badge 2: Consistent Saver (savings score maxed)
        if (currentScore >= 400 &&
                !rewardRepository.existsByUserIdAndBadgeType(userId, Reward.BadgeType.CONSISTENT_SAVER)) {
            newBadges.add(awardBadge(user, "Consistent Saver", Reward.BadgeType.CONSISTENT_SAVER, 0));
        }

        // Badge 3: Score Milestone — reached 500
        if (currentScore >= 500 &&
                !rewardRepository.existsByUserIdAndBadgeType(userId, Reward.BadgeType.SCORE_MILESTONE)) {
            newBadges.add(awardBadge(user, "Score Milestone: 500", Reward.BadgeType.SCORE_MILESTONE, 0));
        }

        // Badge 4: Streak — 4+ weeks logging
        Long weeks = transactionRepository.countDistinctWeeksByUserId(userId);
        if (weeks != null && weeks >= 4 &&
                !rewardRepository.existsByUserIdAndBadgeType(userId, Reward.BadgeType.STREAK_WEEK)) {
            newBadges.add(awardBadge(user, "4-Week Streak", Reward.BadgeType.STREAK_WEEK, weeks.intValue()));
        }

        // Badge 5: Platinum tier
        if (currentScore >= 750 && !rewardRepository.existsByUserIdAndBadgeType(userId, Reward.BadgeType.PLATINUM_TIER)) {
            newBadges.add(awardBadge(user, "Platinum Achiever", Reward.BadgeType.PLATINUM_TIER, 0));
        }

        return newBadges;
    }

    private Reward awardBadge(User user, String name, Reward.BadgeType type, int streak) {
        return rewardRepository.save(Reward.builder()
                .user(user)
                .badgeName(name)
                .badgeType(type)
                .streakCount(streak)
                .build());
    }

    public List<Reward> getMyRewards(UUID userId) {
        return rewardRepository.findByUserIdOrderByEarnedAtDesc(userId);
    }
}
