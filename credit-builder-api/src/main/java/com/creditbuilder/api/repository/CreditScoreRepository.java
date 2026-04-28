package com.creditbuilder.api.repository;

import com.creditbuilder.api.entity.CreditScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditScoreRepository extends JpaRepository<CreditScore, UUID> {

    // Most recent score for a user
    Optional<CreditScore> findTopByUserIdOrderByCalculatedAtDesc(UUID userId);

    // Full score history for user
    List<CreditScore> findByUserIdOrderByCalculatedAtAsc(UUID userId);
}
