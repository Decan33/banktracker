package com.banktracker.util;

import com.banktracker.exceptions.InvalidCsvRowException;
import com.banktracker.model.TransactionType;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Set;

@Slf4j
public class TransactionRowValidator {
    public static final String IBAN_REGEX_PATTERN = "^[A-Z]{2}[0-9A-Z]{13,32}$";
    private final NamedCsvRecord record;

    public TransactionRowValidator(NamedCsvRecord record) {
        this.record = record;
    }

    public void validateRecord() {
        validateIban(record.getField("iban"));
        validateYearMonth(record.getField("transactionDate"));
        validateCurrency(record.getField("currency"));
        validateTransactionType(record.getField("transactionType"));
        validateAmount(record.getField("amount"));
    }

    private void validateIban(String iban) {
        if (iban == null || iban.isBlank()) {
            throw new InvalidCsvRowException("IBAN is required");
        }

        if (!iban.matches(IBAN_REGEX_PATTERN)) {
            throw new InvalidCsvRowException("Invalid IBAN format: " + iban);
        }
    }

    private void validateYearMonth(String value) {
        try {
            YearMonth.parse(value);
        } catch (DateTimeParseException e) {
            throw new InvalidCsvRowException(
                    "Invalid transactionDate: " + value
            );
        }
    }

    private void validateCurrency(String currency) {
        Set<String> supported =
                Set.of("PLN");

        if (!supported.contains(currency)) {
            throw new InvalidCsvRowException(
                    "Unsupported currency: " + currency
            );
        }

    }

    private void validateTransactionType(String value) {
        try {
            TransactionType.valueOf(value);
        } catch (IllegalArgumentException e) {
            throw new InvalidCsvRowException(
                    "Invalid transaction type: " + value
            );
        }
    }

    private void validateAmount(String value) {
        try {
            new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new InvalidCsvRowException(
                    "Invalid amount: " + value
            );
        }
    }
}
