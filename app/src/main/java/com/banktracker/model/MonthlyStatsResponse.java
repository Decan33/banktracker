package com.banktracker.model;

import java.math.BigDecimal;

public record MonthlyStatsResponse(
        String month,
        long transactionCount,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {
}
