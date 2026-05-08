package com.banktracker.service;

import com.banktracker.model.response.CategoryStatsResponse;
import com.banktracker.model.response.MonthlyStatsResponse;
import com.banktracker.model.response.TransactionResponse;
import com.banktracker.model.TransactionType;
import com.banktracker.repository.TransactionStatisticsRepository;
import com.banktracker.util.TransactionMapper;
import com.banktracker.aggregation.TransactionStatsAggregationFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionsStatisticsService {
    private final MongoTemplate mongoTemplate;
    private final TransactionStatsAggregationFactory aggregationFactory;

    private final TransactionStatisticsRepository bankTransactionRepository;

    public List<CategoryStatsResponse> getCategories(String iban) {

        var aggregation = aggregationFactory.categoryStats(iban);

        return mongoTemplate.aggregate(aggregation, "bank_transactions", CategoryStatsMongoResult.class)
                .getMappedResults()
                .stream().map(
                        CategoryStatsMongoResult::toResponse
                )
                .collect(Collectors.toList());

    }

    public List<MonthlyStatsResponse> getStatsByMonth(YearMonth from, YearMonth to, String iban) {
        var aggregation = aggregationFactory.getMonthlyStats(from, to, iban);

        return mongoTemplate
                .aggregate(aggregation, "bank_transactions", MonthlyStatsMongoResult.class)
                .getMappedResults()
                .stream()
                .map(MonthlyStatsMongoResult::toResponse)
                .toList();
    }

    public Page<TransactionResponse> getAllTransactions(Pageable pageable) {
        return bankTransactionRepository.findAll(pageable).map(TransactionMapper::toResponse);
    }

    public Page<TransactionResponse> getTransactionsWithIban(String iban, Pageable pageable) {
        return bankTransactionRepository.findByIban(iban, pageable).map(TransactionMapper::toResponse);
    }

    private record CategoryStatsMongoResult(
            TransactionType type,
            String month,
            long transactionCount,
            BigDecimal income,
            BigDecimal expense,
            BigDecimal net
    ) {
        CategoryStatsResponse toResponse() {
            return new CategoryStatsResponse(
                    type,
                    month,
                    transactionCount,
                    income,
                    expense,
                    net
            );
        }
    }

    private record MonthlyStatsMongoResult(
            String month,
            long transactionCount,
            BigDecimal income,
            BigDecimal expense,
            BigDecimal net
    ) {
        MonthlyStatsResponse toResponse() {
            return new MonthlyStatsResponse(
                    month,
                    transactionCount,
                    income,
                    expense,
                    net
            );
        }
    }
}
