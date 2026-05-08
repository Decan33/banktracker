package com.banktracker.service;

import com.banktracker.base.IntegrationTestsBase;
import com.banktracker.model.TransactionDocument;
import com.banktracker.model.TransactionType;
import com.banktracker.repository.TransactionStatisticsRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

class TransactionsStatisticsServiceIntegrationTest
        extends IntegrationTestsBase {

    @Autowired
    private TransactionStatisticsRepository repository;

    @Autowired
    private TransactionsStatisticsService service;

    @Test
    void shouldReturnStatsForEveryAvailableMonth() {
        repository.save(TransactionDocument.builder()
                .iban("PL111")
                .transactionDate("2024-01")
                .currency("PLN")
                .transactionType(TransactionType.SALARY)
                .amount(new BigDecimal("5000.00"))
                .build());

        repository.save(TransactionDocument.builder()
                .iban("PL111")
                .transactionDate("2024-01")
                .currency("PLN")
                .transactionType(TransactionType.GROCERIES)
                .amount(new BigDecimal("-500.00"))
                .build());

        repository.save(TransactionDocument.builder()
                .iban("PL222")
                .transactionDate("2024-02")
                .currency("PLN")
                .transactionType(TransactionType.RENT)
                .amount(new BigDecimal("-2000.00"))
                .build());

        var result = service.getStatsByMonth(null, null);

        Assertions.assertThat(result).hasSize(2);

        Assertions.assertThat(result.get(0).month()).isEqualTo("2024-01");
        Assertions.assertThat(result.get(0).transactionCount()).isEqualTo(2);
        Assertions.assertThat(result.get(0).income()).isEqualByComparingTo("5000.00");
        Assertions.assertThat(result.get(0).expense()).isEqualByComparingTo("500.00");
        Assertions.assertThat(result.get(0).net()).isEqualByComparingTo("4500.00");

        Assertions.assertThat(result.get(1).month()).isEqualTo("2024-02");
        Assertions.assertThat(result.get(1).transactionCount()).isEqualTo(1);
        Assertions.assertThat(result.get(1).income()).isEqualByComparingTo("0");
        Assertions.assertThat(result.get(1).expense()).isEqualByComparingTo("2000.00");
        Assertions.assertThat(result.get(1).net()).isEqualByComparingTo("-2000.00");
    }

    @Test
    void shouldFilterMonthlyStatsByDateRange() {
        repository.save(TransactionDocument.builder()
                .iban("PL111")
                .transactionDate("2024-01")
                .currency("PLN")
                .transactionType(TransactionType.SALARY)
                .amount(new BigDecimal("1000.00"))
                .build());

        repository.save(TransactionDocument.builder()
                .iban("PL111")
                .transactionDate("2024-02")
                .currency("PLN")
                .transactionType(TransactionType.SALARY)
                .amount(new BigDecimal("2000.00"))
                .build());

        repository.save(TransactionDocument.builder()
                .iban("PL222")
                .transactionDate("2024-02")
                .currency("PLN")
                .transactionType(TransactionType.SALARY)
                .amount(new BigDecimal("9999.00"))
                .build());

        var result = service.getStatsByMonth(
                java.time.YearMonth.of(2024, 2),
                java.time.YearMonth.of(2024, 2)
        );

        Assertions.assertThat(result).hasSize(3);
        Assertions.assertThat(result.get(0).month()).isEqualTo("2024-02");
        Assertions.assertThat(result.get(0).income()).isEqualByComparingTo("2000.00");
    }

    @Test
    void shouldReturnCategoryStats() {
        repository.save(TransactionDocument.builder()
                .iban("PL111")
                .transactionDate("2024-01")
                .currency("PLN")
                .transactionType(TransactionType.GROCERIES)
                .amount(new BigDecimal("-100.00"))
                .build());

        repository.save(TransactionDocument.builder()
                .iban("PL111")
                .transactionDate("2024-01")
                .currency("PLN")
                .transactionType(TransactionType.GROCERIES)
                .amount(new BigDecimal("-50.00"))
                .build());

        repository.save(TransactionDocument.builder()
                .iban("PL111")
                .transactionDate("2024-01")
                .currency("PLN")
                .transactionType(TransactionType.SALARY)
                .amount(new BigDecimal("5000.00"))
                .build());

        var result = service.getCategories();

        Assertions.assertThat(result).hasSize(2);

        var groceries = result.stream()
                .filter(r -> r.type() == TransactionType.GROCERIES)
                .findFirst()
                .orElseThrow();

        Assertions.assertThat(groceries.transactionCount()).isEqualTo(2);
        Assertions.assertThat(groceries.expense()).isEqualByComparingTo("150.00");
        Assertions.assertThat(groceries.income()).isEqualByComparingTo("0");
        Assertions.assertThat(groceries.net()).isEqualByComparingTo("-150.00");
    }
}
