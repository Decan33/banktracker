package com.banktracker.model.response;

import java.math.BigDecimal;

public record CategoryStatsMongoResult(
        String month,
        long transactionCount,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {
    public CategoryMonthlyStatsResponse toResponse() {
        return new CategoryMonthlyStatsResponse(
                month,
                transactionCount,
                income,
                expense,
                net
        );
    }
}
