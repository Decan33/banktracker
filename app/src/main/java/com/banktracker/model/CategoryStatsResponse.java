package com.banktracker.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public record CategoryStatsResponse(
        TransactionType type,
        YearMonth month,
        long transactionCount,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {}
