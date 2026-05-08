package com.banktracker.repository;

import com.banktracker.model.TransactionDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionStatisticsRepository extends MongoRepository<TransactionDocument, String> {
    Page<TransactionDocument> findByIban(String iban, Pageable pageable);
}
