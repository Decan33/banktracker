package com.banktracker.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionStatsAggregationFactoryTest {

    private final TransactionStatsAggregationFactory factory =
            new TransactionStatsAggregationFactory();

    @Test
    void categoryStatsShouldContainAggregationStages() {
        Aggregation aggregation = factory.categoryStats(null);

        var pipeline = aggregation.toPipeline(
                org.springframework.data.mongodb.core.aggregation.Aggregation.DEFAULT_CONTEXT
        );

        assertThat(pipeline).hasSizeGreaterThanOrEqualTo(3);
    }

    @Test
    void categoryStatsWithIbanShouldStartWithMatchStage() {
        Aggregation aggregation = factory.categoryStats("PL123");

        var pipeline = aggregation.toPipeline(
                org.springframework.data.mongodb.core.aggregation.Aggregation.DEFAULT_CONTEXT
        );

        assertThat(pipeline.get(0).toJson()).contains("$match");
        assertThat(pipeline.get(0).toJson()).contains("PL123");
    }

    @Test
    void monthlyStatsWithoutFiltersShouldNotContainEmptyAnd() {
        Aggregation aggregation = factory.getMonthlyStats(null, null, null);

        var pipeline = aggregation.toPipeline(
                org.springframework.data.mongodb.core.aggregation.Aggregation.DEFAULT_CONTEXT
        );

        assertThat(pipeline.toString()).doesNotContain("\"$and\": []");
    }
}
