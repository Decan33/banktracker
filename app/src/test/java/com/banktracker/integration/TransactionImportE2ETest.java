package com.banktracker.integration;

import java.nio.charset.StandardCharsets;

import org.iban4j.CountryCode;
import org.iban4j.Iban;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.banktracker.base.E2EIntegrationTestBase;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionImportE2ETest
                extends E2EIntegrationTestBase {

        @Test
        void shouldImportValidCsv() throws Exception {
                String csv = """
                                iban,transactionDate,currency,transactionType,amount
                                %s,2024-01,PLN,SALARY,5000.00
                                %s,2024-01,PLN,GROCERIES,-120.50
                                """;

                String iban1 = Iban.random(CountryCode.PL).toString();
                csv = String.format(csv, iban1, iban1);

                MockMultipartFile file = new MockMultipartFile(
                                "csv",
                                "transactions.csv",
                                "text/csv",
                                csv.getBytes(StandardCharsets.UTF_8));

                mockMvc.perform(
                                multipart(
                                                TRANSACTIONS_IMPORT_ENDPOINT)
                                                .file(file)
                                                .param(
                                                                "month",
                                                                "2024-01"))
                                .andExpect(
                                                status().isOk())
                                .andExpect(
                                                jsonPath(
                                                                "$.status").value("COMPLETED"))
                                .andExpect(
                                                jsonPath(
                                                                "$.importedRows").value(2))
                                .andExpect(
                                                jsonPath(
                                                                "$.skippedRows").value(0));
        }

        @Test
        void shouldCompleteWithErrorsForInvalidRows() throws Exception {
                String csv = """
                                iban,transactionDate,currency,transactionType,amount
                                %s,2024-01,PLN,SALARY,5000.00
                                %s,wrong-date,PLN,GROCERIES,-120.50
                                %s,2024-01,PLN,INVALID_TYPE,-120.50
                                """;

                String iban1 = Iban.random(CountryCode.PL).toString();
                csv = String.format(csv, iban1, iban1, iban1);

                MockMultipartFile file = new MockMultipartFile(
                                "csv",
                                "transactions.csv",
                                "text/csv",
                                csv.getBytes(StandardCharsets.UTF_8));

                mockMvc.perform(multipart(TRANSACTIONS_IMPORT_ENDPOINT)
                                .file(file))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("COMPLETED_WITH_ERRORS"))
                                .andExpect(jsonPath("$.importedRows").value(1))
                                .andExpect(jsonPath("$.skippedRows").value(2))
                                .andExpect(jsonPath("$.errors").isArray());
        }

        @Test
        void shouldRejectEmptyFile() throws Exception {
                MockMultipartFile file = new MockMultipartFile(
                                "csv",
                                "transactions.csv",
                                "text/csv",
                                new byte[0]);

                mockMvc.perform(multipart(TRANSACTIONS_IMPORT_ENDPOINT)
                                .file(file)
                                .param("month", "2024-01"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("CSV_IMPORT_ERROR"));
        }

        @Test
        void shouldRejectDuplicateImport() throws Exception {
                String csv = """
                                iban,transactionDate,currency,transactionType,amount
                                %s,2024-01,PLN,SALARY,5000.00
                                """;

                String iban1 = Iban.random(CountryCode.PL).toString();
                csv = String.format(csv, iban1);

                MockMultipartFile file1 = new MockMultipartFile(
                                "csv",
                                "transactions.csv",
                                "text/csv",
                                csv.getBytes(StandardCharsets.UTF_8));

                MockMultipartFile file2 = new MockMultipartFile(
                                "csv",
                                "transactions.csv",
                                "text/csv",
                                csv.getBytes(StandardCharsets.UTF_8));

                mockMvc.perform(multipart(TRANSACTIONS_IMPORT_ENDPOINT)
                                .file(file1)
                                .param("month", "2024-01"))
                                .andExpect(status().isOk());

                mockMvc.perform(multipart(TRANSACTIONS_IMPORT_ENDPOINT)
                                .file(file2)
                                .param("month", "2024-01"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").value("CSV_IMPORT_ERROR"));
        }
}