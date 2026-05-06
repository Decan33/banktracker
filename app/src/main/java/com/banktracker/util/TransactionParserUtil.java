package com.banktracker.util;

import com.banktracker.model.BankingTransactionInfo;
import com.banktracker.model.TransactionType;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRecord;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;
import java.util.stream.Stream;

public class TransactionParserUtil {
    CsvReader<NamedCsvRecord> records;
    UUID sessionId;

    public TransactionParserUtil(CsvReader<NamedCsvRecord> records, UUID sessionId) {
        this.records = records;
        this.sessionId = sessionId;
    }

    public Stream<BankingTransactionInfo> mapBankingTransactionInfo() {
        return records.stream().map(this::mapInfo);
    }

    private BankingTransactionInfo mapInfo(final NamedCsvRecord record) {
        return BankingTransactionInfo.builder()
                .importId(sessionId.toString())
                .iban(record.getField("iban"))
                .transactionDate(YearMonth.parse(record.getField("transactionDate")))
                .currency(record.getField("currency"))
                .transactionType(TransactionType.valueOf(record.getField("transactionType")))
                .amount(new BigDecimal(record.getField("amount")))
                .build();
    }

}
