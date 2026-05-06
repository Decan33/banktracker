package com.banktracker.model;

import java.util.UUID;

public record ImportTransactionResponse(UUID importTransactionId, ImportStatus status, Integer importedRows, Integer skippedRows) {
}
