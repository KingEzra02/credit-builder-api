package com.creditbuilder.api.repository;

import com.creditbuilder.api.entity.LoanApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, UUID> {

    List<LoanApplication> findByUserIdOrderByAppliedAtDesc(UUID userId);
    long countByUserIdAndStatus(UUID userId, LoanApplication.LoanStatus status);
}
