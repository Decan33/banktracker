package com.banktracker.service;

import com.banktracker.model.CategoryStatsResponse;
import com.banktracker.model.TransactionType;
import com.banktracker.util.TransactionStatsAggregationFactory;
import lombok.RequiredArgsConstructor;
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

    public List<CategoryStatsResponse> getCategories(YearMonth from, YearMonth to, String iban) {

        var aggregation = aggregationFactory.categoryStats(from, to, iban);

        return mongoTemplate.aggregate(aggregation, "bank_transactions", CategoryStatsMongoResult.class)
                .getMappedResults()
                .stream().map(
                        CategoryStatsMongoResult::toResponse
                )
                .collect(Collectors.toList());

    }

    private record CategoryStatsMongoResult(
            TransactionType type,
            YearMonth month,
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
}
