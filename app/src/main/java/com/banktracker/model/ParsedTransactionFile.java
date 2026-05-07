package com.banktracker.model;

import java.util.List;

public record ParsedTransactionFile(
        List<BankingTransactionInfo> transactions,
        List<ParsingErrorInfo> errors
) {
}
