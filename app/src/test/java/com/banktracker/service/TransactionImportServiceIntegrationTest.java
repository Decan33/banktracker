package com.banktracker.service;

import com.banktracker.base.IntegrationTestsBase;
import com.banktracker.model.ImportStatus;
import com.banktracker.repository.TransactionImportRepository;
import com.banktracker.repository.TransactionStatisticsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;

import org.iban4j.CountryCode;
import org.iban4j.Iban;

class TransactionImportServiceIntegrationTest
                extends IntegrationTestsBase {

        @Autowired
        private TransactionsImportService service;

        @Autowired
        private TransactionStatisticsRepository transactionRepository;

        @Autowired
        private TransactionImportRepository importRepository;

        @Test
        void shouldImportCsvAndSaveImportMetadata() {
                String csv = """
                                iban,transactionDate,currency,transactionType,amount
                                %s,2024-01,PLN,SALARY,5000.00
                                %s,2024-01,PLN,GROCERIES,-100.00
                                """;

                String iban1 = Iban.random(CountryCode.PL).toString();
                String iban2 = Iban.random(CountryCode.PL).toString();

                csv = String.format(csv, iban1, iban2);

                MockMultipartFile file = new MockMultipartFile(
                                "csv",
                                "transactions.csv",
                                "text/csv",
                                csv.getBytes());

                var response = service.importTransaction(
                                file);

                assertThat(response.status()).isEqualTo(ImportStatus.COMPLETED);
                assertThat(response.importedRows()).isEqualTo(2);
                assertThat(response.skippedRows()).isZero();

                assertThat(transactionRepository.findAll()).hasSize(2);
                assertThat(importRepository.findAll()).hasSize(1);

                var importBatch = importRepository.findAll().get(0);

                assertThat(importBatch.getImportStatus()).isEqualTo(ImportStatus.COMPLETED);
                assertThat(importBatch.getImportedRows()).isEqualTo(2);
                assertThat(importBatch.getSkippedRows()).isZero();
        }

        @Test
        void shouldCompleteWithErrorsWhenCsvContainsInvalidRows() {
                String csv = """
                                iban,transactionDate,currency,transactionType,amount
                                %s,2024-01,PLN,SALARY,5000.00
                                %s,wrong,PLN,SALARY,5000.00
                                %s,2024-01,PLN,INVALID_TYPE,-100.00
                                """;
                String ibanString = Iban.random(CountryCode.PL).toString();

                csv = String.format(csv, ibanString, ibanString, ibanString);

                MockMultipartFile file = new MockMultipartFile(
                                "csv",
                                "transactions.csv",
                                "text/csv",
                                csv.getBytes());

                var response = service.importTransaction(
                                file);

                assertThat(response.status()).isEqualTo(ImportStatus.COMPLETED_WITH_ERRORS);
                assertThat(response.importedRows()).isEqualTo(1);
                assertThat(response.skippedRows()).isEqualTo(2);
                assertThat(response.errors()).hasSize(2);

                assertThat(transactionRepository.findAll()).hasSize(1);

                var importBatch = importRepository.findAll().get(0);

                assertThat(importBatch.getImportStatus())
                                .isEqualTo(ImportStatus.COMPLETED_WITH_ERRORS);
        }
}
