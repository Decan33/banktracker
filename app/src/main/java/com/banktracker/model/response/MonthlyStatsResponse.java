package com.banktracker.model.response;

import java.math.BigDecimal;

public record MonthlyStatsResponse(
        String month,
        long transactionCount,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {
}
