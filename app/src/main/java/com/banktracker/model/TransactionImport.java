package com.banktracker.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Builder
@Setter
@Getter
@Document("transaction_imports")
@CompoundIndex(
        name = "uniq_checksum",
        def = "{'checksum': 1}",
        unique = true
)
public class TransactionImport {
    @Id
    private String id;
    private final String filename;
    private final String checksum;
    private ImportStatus importStatus;
    private final Instant importTime;
    Integer skippedRows;
    Integer importedRows;
    String errorMessage;
}

