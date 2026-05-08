package com.banktracker.model;

import com.banktracker.model.error.ParsingErrorInfo;

import java.util.List;

public record ParsedTransactionFile(
        List<TransactionDocument> transactions,
        List<ParsingErrorInfo> errors
) {
}
