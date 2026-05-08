package com.banktracker.model.response;

import java.math.BigDecimal;

public record MonthlyStatsMongoResult(
        String month,
        long transactionCount,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {
    public MonthlyStatsResponse toResponse() {
        return new MonthlyStatsResponse(
                month,
                transactionCount,
                income,
                expense,
                net
        );
    }
}
