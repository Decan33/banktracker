package com.banktracker.service;

import com.banktracker.exceptions.CsvImportException;
import com.banktracker.model.ImportStatus;
import com.banktracker.model.response.ImportTransactionResponse;
import com.banktracker.model.ParsedTransactionFile;
import com.banktracker.model.TransactionDocument;
import com.banktracker.model.TransactionImport;
import com.banktracker.repository.TransactionImportRepository;
import com.banktracker.repository.TransactionStatisticsRepository;
import com.banktracker.util.CsvTransactionParser;
import com.banktracker.util.FileChecksumService;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.FieldMismatchStrategy;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionsImportService {
    private final TransactionStatisticsRepository transactionRepository;
    private final TransactionImportRepository transactionImportRepository;
    private final FileChecksumService fileChecksumService;

    public ImportTransactionResponse importTransaction(MultipartFile csvFile) {
        validateFile(csvFile);

        String checksum = fileChecksumService.sha256(csvFile);

        if (transactionImportRepository.existsByChecksum(checksum)) {
            throw new CsvImportException(
                    "This CSV file was already imported"
            );
        }

        var sessionId = UUID.randomUUID().toString();
        log.info("Starting importing transaction with NEW sessionId: {}", sessionId);
        var importedTransaction = TransactionImport
                .builder()
                .id(sessionId)
                .filename(csvFile.getOriginalFilename())
                .checksum(checksum)
                .importStatus(ImportStatus.NEW)
                .importTime(Instant.now())
                .build();

        var t = transactionImportRepository.save(importedTransaction);

        ParsedTransactionFile transactionInfos;
        try {
            transactionInfos = parseTransactionFile(csvFile, t.getId());
            ImportStatus finalStatus = transactionInfos.errors().isEmpty()
                    ? ImportStatus.COMPLETED
                    : ImportStatus.COMPLETED_WITH_ERRORS;
            transactionRepository.saveAll(transactionInfos.transactions());

            t.setImportStatus(finalStatus);
            t.setImportedRows(transactionInfos.transactions().size());
            t.setSkippedRows(transactionInfos.errors().size());

            transactionImportRepository.save(t);

            var errors = transactionInfos.errors();

            return new ImportTransactionResponse(
                    t.getId(),
                    finalStatus,
                    transactionInfos.transactions().size(),
                    errors.size(),
                    errors
            );

        } catch (IOException e) {
            log.error("There is a FAILED importing session with sessionId: {}", sessionId);
            t.setImportStatus(ImportStatus.FAILED);
            t.setErrorMessage(e.getMessage());
            transactionImportRepository.save(t);

            throw new CsvImportException("Could not parse CSV file", e);
        }


    }

    private ParsedTransactionFile parseTransactionFile(
            MultipartFile csvFile,
            String sessionId
    ) throws IOException {

        try (
                CsvReader<NamedCsvRecord> csv = CsvReader
                        .builder()
                        .missingFieldStrategy(FieldMismatchStrategy.SKIP)
                        .extraFieldStrategy(FieldMismatchStrategy.SKIP)
                        .ofNamedCsvRecord(csvFile.getInputStream())
        ) {

            CsvTransactionParser parser =
                    new CsvTransactionParser(csv, sessionId);

            List<TransactionDocument> parsed =
                    parser.parse();

            return new ParsedTransactionFile(
                    parsed,
                    parser.getErrors()
            );
        }
    }

    private void validateFile(MultipartFile file) {

        if (file.isEmpty()) {
            throw new CsvImportException(
                    "Uploaded file is empty"
            );
        }
        String filename = file.getOriginalFilename();

        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new CsvImportException("Only CSV files are supported");
        }

        long maxSize = 50 * 1024 * 1024;

        if (file.getSize() > maxSize) {
            throw new CsvImportException(
                    "CSV file exceeds maximum size"
            );
        }
    }
}
