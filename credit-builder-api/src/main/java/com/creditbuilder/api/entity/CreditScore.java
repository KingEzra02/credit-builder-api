package com.creditbuilder.api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditScore {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer score;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CreditLevel level;

    // Storing the breakdown as jso,
    // {"savingsScore": 120, "streakScore": 80, "consistencyScore": 150}

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "score_breakdown", columnDefinition = "jsonb")
    private Map<String, Integer> scoreBreakdown;

    @CreationTimestamp
    @Column(name = "calculated_at", updatable = false)
    private LocalDateTime calculatedAt;

    public enum CreditLevel {
        BEGINNER,
        BRONZE,
        SILVER,
        GOLD,
        PLATINUM
    }

    // DERIVING LEVEL FROM SCORE

    public static CreditLevel calculateLevel(int score){
        if(score >= 750) return CreditLevel.PLATINUM;
        if(score >= 650) return CreditLevel.GOLD;
        if(score >= 550) return CreditLevel.SILVER;
        if (score >= 450) return  CreditLevel.BRONZE;
        return CreditLevel.BEGINNER;
    }

}
