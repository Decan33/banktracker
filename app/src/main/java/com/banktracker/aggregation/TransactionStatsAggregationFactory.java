package com.banktracker.aggregation;

import com.banktracker.exceptions.DateConflictException;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionStatsAggregationFactory {
    public Aggregation categoryStats(@Nullable String iban) {
        List<AggregationOperation> operations = new ArrayList<>();

        if (iban != null && !iban.isBlank()) {
            operations.add(Aggregation.match(Criteria.where("iban").is(iban)));
        }

        operations.add(Aggregation.project("transactionType", "transactionDate", "amount")
                .andExpression("cond(amount > 0, amount, 0)").as("income")
                .andExpression("cond(amount < 0, abs(amount), 0)").as("expense"));

        operations.add(Aggregation.group("transactionType", "transactionDate")
                .count().as("transactionCount")
                .sum("income").as("income")
                .sum("expense").as("expense"));

        operations.add(Aggregation.project("transactionCount", "income", "expense")
                .and("_id.transactionType").as("type")
                .and("_id.transactionDate").as("month")
                .andExpression("income - expense").as("net")
                .andExclude("_id"));


        return Aggregation.newAggregation(operations);
    }

    public Aggregation getMonthlyStats(YearMonth from, YearMonth to, String iban) {
        List<AggregationOperation> operations = new ArrayList<>();

        if (iban != null && !iban.isBlank()) {
            operations.add(
                    Aggregation.match(Criteria.where("iban").is(iban))
            );
        }

        addMonthlyFilter(from, to, operations);

        operations.add(
                Aggregation.project("transactionDate", "amount")
                        .andExpression("cond(amount > 0, amount, 0)").as("income")
                        .andExpression("cond(amount < 0, abs(amount), 0)").as("expense")
        );

        operations.add(
                Aggregation.group("transactionDate")
                        .count().as("transactionCount")
                        .sum("income").as("income")
                        .sum("expense").as("expense")
        );

        operations.add(
                Aggregation.project("transactionCount", "income", "expense")
                        .and("_id").as("month")
                        .andExpression("income - expense").as("net")
                        .andExclude("_id")
        );

        operations.add(
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "month"))
        );

        return Aggregation.newAggregation(operations);
    }

    private void addMonthlyFilter(
            YearMonth from,
            YearMonth to,
            List<AggregationOperation> operations
    ) {
        if (from == null && to == null) {
            return;
        }

        if (from != null && to != null && from.isAfter(to)) {
            throw new DateConflictException("'from' month must be before or equal to 'to' month");
        }

        Criteria criteria = Criteria.where("transactionDate");

        if (from != null) {
            criteria = criteria.gte(from.toString());
        }

        if (to != null) {
            criteria = criteria.lte(to.toString());
        }

        operations.add(Aggregation.match(criteria));
    }
}
