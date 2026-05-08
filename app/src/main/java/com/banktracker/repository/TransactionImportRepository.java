package com.banktracker.repository;

import com.banktracker.model.TransactionImport;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionImportRepository
        extends MongoRepository<TransactionImport, String> {

    boolean existsByChecksum(String checksum);
}
