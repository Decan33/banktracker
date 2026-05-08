package com.banktracker.util;

import com.banktracker.aggregation.TransactionStatsAggregationFactory;
import com.banktracker.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionStatsAggregationFactoryTest {

    private final TransactionStatsAggregationFactory factory =
            new TransactionStatsAggregationFactory();

    @Test
    void categoryStatsShouldContainAggregationStages() {
        Aggregation aggregation = factory.categoryStats(TransactionType.OTHER);

        var pipeline = aggregation.toPipeline(
                org.springframework.data.mongodb.core.aggregation.Aggregation.DEFAULT_CONTEXT
        );

        assertThat(pipeline).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void monthlyStatsWithoutFiltersShouldNotContainEmptyAnd() {
        Aggregation aggregation = factory.getMonthlyStats(null, null);

        var pipeline = aggregation.toPipeline(
                org.springframework.data.mongodb.core.aggregation.Aggregation.DEFAULT_CONTEXT
        );

        assertThat(pipeline.toString()).doesNotContain("\"$and\": []");
    }
}
