package com.creditbuilder.api.repository;

import com.creditbuilder.api.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    // All transaction for user, newest first
    List<Transaction> findByUserIdOrderByDateDesc(UUID userId);

    // Transaction within a date range

    List<Transaction> findByUserIdAndDateBetween(
            UUID userId, LocalDate start, LocalDate end
    );

    // Sum of all income or expensive for a user
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
            "WHERE t.user.id = :userId AND t.type = :type")
    BigDecimal sumByUserIdAndType(
            @Param("userId") UUID userId,
            @Param("type") Transaction.TransactionType type
    );

    // Count distinct weeks user has logged transactions
    @Query("SELECT COUNT(DISTINCT FUNCTION('DATE_TRUNC', 'week', t.date)) " +
            "FROM Transaction t WHERE t.user.id = :userId")
    Long countDistinctWeeksByUserId(@Param("userId") UUID userId);

    // Get transaction grouped by month for scoring
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
            "AND t.date >= :since ORDER BY t.date ASC")
    List<Transaction> findByUserIdSince(
            @Param("userId") UUID userId,
            @Param("since") LocalDate since
    );
}
