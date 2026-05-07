package com.banktracker.util;

import com.banktracker.exceptions.InvalidCsvRowException;
import com.banktracker.model.BankingTransactionInfo;
import com.banktracker.model.ParsingErrorInfo;
import com.banktracker.model.TransactionType;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class TransactionParserUtil {
    CsvReader<NamedCsvRecord> records;
    String sessionId;
    List<ParsingErrorInfo> errorInfos = new ArrayList<>();
    public static final int MAX_ERRORS = 1000;

    public TransactionParserUtil(CsvReader<NamedCsvRecord> records, String sessionId) {
        this.records = records;
        this.sessionId = sessionId;
    }

    public List<BankingTransactionInfo> parse() {
        var result = new ArrayList<BankingTransactionInfo>();

        for (var record : records) {
            try {

                result.add(mapInfo(record));

            } catch (InvalidCsvRowException e) {

                if (errorInfos.size() < MAX_ERRORS) {
                    errorInfos.add(
                            new ParsingErrorInfo(
                                    record.getStartingLineNumber(),
                                    e.getMessage(),
                                    record.getFields().toString()
                            )
                    );
                }
            }
        }

        return result;
    }

    private BankingTransactionInfo mapInfo(final NamedCsvRecord record) {
        return BankingTransactionInfo.builder()
                .id(null)
                .importId(sessionId)
                .iban(record.getField("iban"))
                .transactionDate(
                        validateYearMonth(record.getField("transactionDate"))
                )
                .currency(record.getField("currency"))
                .transactionType(
                        parseTransactionType(record.getField("transactionType"))
                )
                .amount(
                        parseAmount(record.getField("amount"))
                )
                .build();
    }

    private BigDecimal parseAmount(String value) {
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new InvalidCsvRowException(
                    "Invalid amount: " + value
            );
        }
    }

    private TransactionType parseTransactionType(String value) {
        try {
            return TransactionType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidCsvRowException(
                    "Invalid transaction type: " + value
            );
        }
    }

    private String validateYearMonth(String value) {
        try {
            return YearMonth.parse(value).toString();
        } catch (DateTimeParseException e) {
            throw new InvalidCsvRowException(
                    "Invalid transactionDate: " + value
            );
        }
    }

    public List<ParsingErrorInfo> getErrors() {
        return errorInfos;
    }
}
