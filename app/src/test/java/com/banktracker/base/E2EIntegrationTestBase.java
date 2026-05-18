package com.banktracker.base;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;

@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
public abstract class E2EIntegrationTestBase {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanMongo() {
        mongoTemplate.getDb().drop();
    }

    protected static final String TRANSACTIONS_IMPORT_ENDPOINT = "/api/v1/transactions-import";
    protected static final String TRANSACTIONS_STATISTICS_ENDPOINT = "/api/v1/transactions-statistics";
    protected static final String TRANSACTIONS_BY_CATEGORY_ENDPOINT = TRANSACTIONS_STATISTICS_ENDPOINT + "/categories";
    protected static final String TRANSACTIONS_MONTHLY_ENDPOINT = TRANSACTIONS_STATISTICS_ENDPOINT + "/monthly";
    protected static final String TRANSACTIONS_BY_IBAN_ENDPOINT = TRANSACTIONS_STATISTICS_ENDPOINT + "/iban";
}