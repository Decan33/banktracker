package com.banktracker.service;

import com.banktracker.model.BankingTransactionInfo;
import com.banktracker.model.ImportStatus;
import com.banktracker.model.ImportTransactionResponse;
import com.banktracker.model.TransactionImport;
import com.banktracker.repository.BankTransactionRepository;
import com.banktracker.repository.TransactionImportRepository;
import com.banktracker.util.TransactionParserUtil;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.FieldMismatchStrategy;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionImportService {

    private final BankTransactionRepository transactionRepository;
    private final TransactionImportRepository transactionImportRepository;

    public ImportTransactionResponse importTransaction(MultipartFile csvFile, YearMonth month, String iban) {
        var sessionId = UUID.randomUUID();
        var importedTransaction = TransactionImport
                .builder()
                .filename(csvFile.getOriginalFilename())
                .iban(iban)
                .month(month)
                .importStatus(ImportStatus.NEW)
                .importTime(Instant.now())
                .build();

        var t = transactionImportRepository.save(importedTransaction);

        List<BankingTransactionInfo> transactionInfos;
        try {
            transactionInfos = parseTransactionFile(csvFile, sessionId);
            transactionRepository.saveAll(transactionInfos);

            t.setImportStatus(ImportStatus.COMPLETED);
            t.setImportedRows(transactionInfos.size());
            t.setSkippedRows(0);

            transactionImportRepository.save(t);

            return new ImportTransactionResponse(
                    sessionId,
                    ImportStatus.COMPLETED,
                    transactionInfos.size(),
                    0
            );

        } catch (IOException e) {
            t.setImportStatus(ImportStatus.FAILED);
            t.setErrorMessage(e.getMessage());
            transactionImportRepository.save(t);

            throw new RuntimeException(e);
        }


    }

    private List<BankingTransactionInfo> parseTransactionFile(MultipartFile csvFile, UUID sessionId) throws IOException {
        List<BankingTransactionInfo> parsedTransactions;
        try(CsvReader<NamedCsvRecord> csv = CsvReader
                .builder()
                .missingFieldStrategy(FieldMismatchStrategy.SKIP)
                .extraFieldStrategy(FieldMismatchStrategy.SKIP)
                .ofNamedCsvRecord(csvFile.getInputStream()
                )) {
            parsedTransactions = new TransactionParserUtil(csv, sessionId).mapBankingTransactionInfo().collect(Collectors.toList());
        }

        return parsedTransactions;
    }
}
