package com.banktracker.util;

import jakarta.annotation.Nullable;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionStatsAggregationFactory {
    public Aggregation categoryStats(
            YearMonth from,
            YearMonth to,
            @Nullable String iban
    ) {
        List<Criteria> criterias = new ArrayList<>();
        criterias.add(Criteria.where("transactionDate").gte(from).lte(to));

        if (iban != null && !iban.isBlank()) {
            criterias.add(Criteria.where("iban").is(iban));
        }

        return Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(criterias)),

                Aggregation.project("transactionType", "month", "amount")
                        .andExpression("cond(amount > 0, amount, 0)").as("income")
                        .andExpression("cond(amount < 0, abs(amount), 0)").as("expense"),

                Aggregation.group("transactionType", "month")
                        .count().as("transactionsCount")
                        .sum("income").as("income")
                        .sum("expense").as("expense"),

                Aggregation.project("transactionsCount", "income", "expense")
                        .and("_id.transactionType").as("type")
                        .and("_id.transactionDate").as("month")
                        .andExpression("income - expense").as("net"));
    }
}
