package com.banktracker.model;

import java.math.BigDecimal;

public record CategoryStatsResponse(
        TransactionType type,
        String month,
        long transactionCount,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {}
