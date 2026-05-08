package com.banktracker.model.response;

import com.banktracker.model.TransactionType;

import java.math.BigDecimal;

public record TransactionResponse(
        String iban,
        String transactionDate,
        String currency,
        TransactionType transactionType,
        BigDecimal amount
) {
}
