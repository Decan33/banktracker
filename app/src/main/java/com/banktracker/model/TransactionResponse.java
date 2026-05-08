package com.banktracker.model;

import java.math.BigDecimal;

public record TransactionResponse(
        String iban,
        String transactionDate,
        String currency,
        TransactionType transactionType,
        BigDecimal amount
) {
}
