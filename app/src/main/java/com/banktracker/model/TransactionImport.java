package com.banktracker.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.YearMonth;

@Builder
@Setter
@Getter
@Document("transaction_imports")
public class TransactionImport {
    @Id
    private String id;
    private final String filename;
    private final String iban;
    private final YearMonth month;
    private ImportStatus importStatus;
    private final Instant importTime;
    Integer skippedRows;
    Integer importedRows;
    String errorMessage;
}

