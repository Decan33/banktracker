package com.banktracker.service;

import com.banktracker.model.BankingTransactionInfo;
import com.banktracker.model.ImportStatus;
import com.banktracker.model.ImportTransactionResponse;
import com.banktracker.model.ParsedTransactionFile;
import com.banktracker.model.TransactionImport;
import com.banktracker.repository.BankTransactionRepository;
import com.banktracker.repository.TransactionImportRepository;
import com.banktracker.util.TransactionParserUtil;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.FieldMismatchStrategy;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionImportService {
    private final BankTransactionRepository transactionRepository;
    private final TransactionImportRepository transactionImportRepository;

    public ImportTransactionResponse importTransaction(MultipartFile csvFile, String iban, YearMonth month) {
        var sessionId = UUID.randomUUID().toString();
        log.info("Starting importing transaction with NEW sessionId: {}", sessionId);
        var importedTransaction = TransactionImport
                .builder()
                .id(sessionId)
                .filename(csvFile.getOriginalFilename())
                .iban(iban)
                .month(month.toString())
                .importStatus(ImportStatus.NEW)
                .importTime(Instant.now())
                .build();

        var t = transactionImportRepository.save(importedTransaction);

        ParsedTransactionFile transactionInfos;
        try {
            transactionInfos = parseTransactionFile(csvFile, t.getId());
            transactionRepository.saveAll(transactionInfos.transactions());

            t.setImportStatus(ImportStatus.COMPLETED);
            t.setImportedRows(transactionInfos.transactions().size());
            t.setSkippedRows(transactionInfos.errors().size());

            transactionImportRepository.save(t);

            var importStatus = transactionInfos.errors().isEmpty() ? ImportStatus.COMPLETED : ImportStatus.COMPLETED_WITH_ERRORS;

            var errors = transactionInfos.errors();

            return new ImportTransactionResponse(
                    t.getId(),
                    importStatus,
                    transactionInfos.transactions().size(),
                    errors.size(),
                    errors
            );

        } catch (IOException e) {
            log.error("There is a FAILED importing session with sessionId: {}", sessionId);
            t.setImportStatus(ImportStatus.FAILED);
            t.setErrorMessage(e.getMessage());
            transactionImportRepository.save(t);

            throw new RuntimeException(e);
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

            TransactionParserUtil parser =
                    new TransactionParserUtil(csv, sessionId);

            List<BankingTransactionInfo> parsed =
                    parser.parse();

            return new ParsedTransactionFile(
                    parsed,
                    parser.getErrors()
            );
        }
    }
}
