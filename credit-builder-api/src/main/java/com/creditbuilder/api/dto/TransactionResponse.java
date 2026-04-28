package com.creditbuilder.api.dto;

import com.creditbuilder.api.entity.Transaction;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class TransactionResponse {

    private UUID id;
    private Transaction.TransactionType type;
    private BigDecimal amount;
    private Transaction.TransactionCategory category;
    private String description;
    private LocalDate date;

    // Convenience method to convert entity to DTO

    public static TransactionResponse from(Transaction transaction){
        return TransactionResponse.builder()
                .id(transaction.getId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .category(transaction.getCategory())
                .description(transaction.getDescription())
                .date(transaction.getDate())
                .build();
    }
}
