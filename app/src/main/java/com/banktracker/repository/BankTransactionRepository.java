package com.banktracker.repository;

import com.banktracker.model.BankingTransactionInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankTransactionRepository extends MongoRepository<BankingTransactionInfo, String> {
    List<BankingTransactionInfo> findAllByOrderByIbanAsc();

    List<BankingTransactionInfo> findByIbanOrderByTransactionDateAsc(String iban);
    Page<BankingTransactionInfo> findByIban(String iban, Pageable pageable);
}
