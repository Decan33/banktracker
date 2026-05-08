package com.banktracker.model;

import java.util.List;

public record ParsedTransactionFile(
        List<TransactionDocument> transactions,
        List<ParsingErrorInfo> errors
) {
}
