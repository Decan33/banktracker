package com.banktracker.repository;

import com.banktracker.model.BankingTransactionInfo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankTransactionRepository extends MongoRepository<BankingTransactionInfo, String> {
}
