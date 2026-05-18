package com.banktracker.integration;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.banktracker.base.E2EIntegrationTestBase;
import com.banktracker.model.TransactionDocument;
import com.banktracker.model.TransactionType;
import com.banktracker.repository.TransactionStatisticsRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionStatisticsE2ETest
                extends E2EIntegrationTestBase {

        @Autowired
        private TransactionStatisticsRepository repository;

        @Test
        void shouldReturnMonthlyStats() throws Exception {
                repository.saveAll(List.of(
                                TransactionDocument.builder()
                                                .iban("PL111")
                                                .transactionDate("2024-01")
                                                .currency("PLN")
                                                .transactionType(TransactionType.SALARY)
                                                .amount(new BigDecimal("5000.00"))
                                                .build(),
                                TransactionDocument.builder()
                                                .iban("PL111")
                                                .transactionDate("2024-01")
                                                .currency("PLN")
                                                .transactionType(TransactionType.GROCERIES)
                                                .amount(new BigDecimal("-200.00"))
                                                .build()));

                mockMvc.perform(get(TRANSACTIONS_MONTHLY_ENDPOINT))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$[0].month").value("2024-01"))
                                .andExpect(jsonPath("$[0].transactionCount").value(2))
                                .andExpect(jsonPath("$[0].income").value(5000.00))
                                .andExpect(jsonPath("$[0].expense").value(200.00))
                                .andExpect(jsonPath("$[0].net").value(4800.00));
        }

        @Test
        void shouldRejectInvalidDateRange() throws Exception {
                mockMvc.perform(get(TRANSACTIONS_MONTHLY_ENDPOINT)
                                .param("from", "2024-12")
                                .param("to", "2024-01"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        void shouldRejectInvalidDateValue() throws Exception {
                mockMvc.perform(get(TRANSACTIONS_MONTHLY_ENDPOINT)
                                .param("from", "ddd"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.error").exists());
        }

        @Test
        void shouldReturnCategoryStatsByMonth() throws Exception {
                repository.saveAll(List.of(
                                TransactionDocument.builder()
                                                .iban("PL111")
                                                .transactionDate("2024-01")
                                                .currency("PLN")
                                                .transactionType(TransactionType.GROCERIES)
                                                .amount(new BigDecimal("-100.00"))
                                                .build(),
                                TransactionDocument.builder()
                                                .iban("PL111")
                                                .transactionDate("2024-02")
                                                .currency("PLN")
                                                .transactionType(TransactionType.GROCERIES)
                                                .amount(new BigDecimal("-300.00"))
                                                .build()));

                mockMvc.perform(get(TRANSACTIONS_BY_CATEGORY_ENDPOINT)
                                .param("category", TransactionType.GROCERIES.name()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$[0].month").value("2024-01"))
                                .andExpect(jsonPath("$[0].expense").value(100.00))
                                .andExpect(jsonPath("$[1].month").value("2024-02"))
                                .andExpect(jsonPath("$[1].expense").value(300.00));
        }
}
