package com.banktracker.service;

import com.banktracker.aggregation.TransactionStatsAggregationFactory;
import com.banktracker.model.TransactionType;
import com.banktracker.model.response.CategoryMonthlyStatsResponse;
import com.banktracker.model.response.CategoryStatsMongoResult;
import com.banktracker.model.response.MonthlyStatsMongoResult;
import com.banktracker.model.response.MonthlyStatsResponse;
import com.banktracker.model.response.TransactionResponse;
import com.banktracker.repository.TransactionStatisticsRepository;
import com.banktracker.util.TransactionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionsStatisticsService {
    private final MongoTemplate mongoTemplate;
    private final TransactionStatsAggregationFactory aggregationFactory;

    private final TransactionStatisticsRepository bankTransactionRepository;

    public List<CategoryMonthlyStatsResponse> getCategories(TransactionType type) {
        var aggregation = aggregationFactory.categoryStats(type);

        return mongoTemplate.aggregate(aggregation, "bank_transactions", CategoryStatsMongoResult.class)
                .getMappedResults()
                .stream().map(
                        CategoryStatsMongoResult::toResponse
                )
                .collect(Collectors.toList());

    }

    public List<MonthlyStatsResponse> getStatsByMonth(YearMonth from, YearMonth to) {
        var aggregation = aggregationFactory.getMonthlyStats(from, to);

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
}
