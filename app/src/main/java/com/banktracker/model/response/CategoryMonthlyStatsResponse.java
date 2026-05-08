package com.banktracker.model.response;

import java.math.BigDecimal;

public record CategoryMonthlyStatsResponse(
        String month,
        long transactionCount,
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {}
