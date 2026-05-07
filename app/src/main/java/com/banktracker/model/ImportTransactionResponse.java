package com.banktracker.model;

import java.util.List;

public record ImportTransactionResponse(String importTransactionId, ImportStatus status, Integer importedRows, Integer skippedRows, List<ParsingErrorInfo> errors) {
}
