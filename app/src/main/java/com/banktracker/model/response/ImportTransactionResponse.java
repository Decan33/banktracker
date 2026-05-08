package com.banktracker.model.response;

import com.banktracker.model.ImportStatus;
import com.banktracker.model.error.ParsingErrorInfo;

import java.util.List;

public record ImportTransactionResponse(String importTransactionId, ImportStatus status, Integer importedRows, Integer skippedRows, List<ParsingErrorInfo> errors) {
}
