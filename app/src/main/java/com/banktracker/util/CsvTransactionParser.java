package com.banktracker.util;

import com.banktracker.exceptions.InvalidCsvRowException;
import com.banktracker.model.TransactionDocument;
import com.banktracker.model.error.ParsingErrorInfo;
import com.banktracker.model.TransactionType;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class CsvTransactionParser {
    private final CsvReader<NamedCsvRecord> records;
    private final String sessionId;
    private final List<ParsingErrorInfo> errorInfos = new ArrayList<>();
    private static final int MAX_ERRORS = 1000;

    public CsvTransactionParser(CsvReader<NamedCsvRecord> records, String sessionId) {
        this.records = records;
        this.sessionId = sessionId;
    }

    public List<TransactionDocument> parse() {
        var result = new ArrayList<TransactionDocument>();

        for (var record : records) {
            try {
                new TransactionRowValidator(record).validateRecord();

                result.add(mapInfo(record));

            } catch (InvalidCsvRowException e) {

                if (errorInfos.size() < MAX_ERRORS) {
                    errorInfos.add(
                            new ParsingErrorInfo(
                                    record.getStartingLineNumber(),
                                    e.getMessage()
                            )
                    );
                }
            }
        }

        return result;
    }

    private TransactionDocument mapInfo(final NamedCsvRecord record) {


        return TransactionDocument.builder()
                .id(null)
                .importId(sessionId)
                .iban(
                        record.getField("iban")
                )
                .transactionDate(
                        record.getField("transactionDate")
                )
                .currency(
                        record.getField("currency"))
                .transactionType(
                        TransactionType.valueOf(record.getField("transactionType"))
                )
                .amount(
                        new BigDecimal(record.getField("amount"))
                )
                .build();
    }

    public List<ParsingErrorInfo> getErrors() {
        return errorInfos;
    }
}
