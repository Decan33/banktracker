package com.banktracker.model;

import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document("bank_transactions")
@Builder
public record BankingTransactionInfo(
        @Id String id,

        String importId,
        String iban,
        String transactionDate,
        String currency,
        TransactionType transactionType,
        BigDecimal amount

) {
}
