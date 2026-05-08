package com.banktracker.model;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document("bank_transactions")
@Builder
public record TransactionDocument(
        @Id String id,

        @Indexed
        String importId,
        @Indexed
        String iban,
        @Indexed
        String transactionDate,
        String currency,
        @Indexed
        TransactionType transactionType,
        BigDecimal amount

) {
}
