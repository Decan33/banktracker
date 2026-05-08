package com.banktracker.util;

import com.banktracker.model.TransactionDocument;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.FieldMismatchStrategy;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvTransactionParserTest {

    @Test
    void shouldParseValidRowsAndCollectInvalidRowsAsErrors() throws Exception {
        String csv = """
                iban,transactionDate,currency,transactionType,amount
                PL94107510605753807963141749,2024-01,PLN,SALARY,5000.00
                PL94107510605753807963141749,not-a-month,PLN,SALARY,5000.00
                PL94107510605753807963141749,2024-01,PLN,WRONG_TYPE,123.45
                PL94107510605753807963141749,2024-01,PLN,GROCERIES,-123.45
                """;

        try (CsvReader<NamedCsvRecord> reader = CsvReader
                .builder()
                .missingFieldStrategy(FieldMismatchStrategy.SKIP)
                .extraFieldStrategy(FieldMismatchStrategy.SKIP)
                .ofNamedCsvRecord(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)))) {

            CsvTransactionParser parser = new CsvTransactionParser(reader, "import-123");

            List<TransactionDocument> transactions = parser.parse();

            Assertions.assertThat(transactions).hasSize(2);
            Assertions.assertThat(parser.getErrors()).hasSize(2);

            Assertions.assertThat(transactions.get(0).importId()).isEqualTo("import-123");
            Assertions.assertThat(transactions.get(0).transactionDate()).isEqualTo("2024-01");
            Assertions.assertThat(transactions.get(0).amount()).isEqualByComparingTo("5000.00");
        }
    }
}
