package com.banktracker.controller;

import com.banktracker.model.response.ImportTransactionResponse;
import com.banktracker.service.TransactionsImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class TransactionsImportController {

    private final TransactionsImportService service;

    @PostMapping("/api/v1/transactions-import")
    public ImportTransactionResponse uploadBankingInformation(@RequestParam("csv") MultipartFile csvFile, @RequestParam String accountIban) {

        return service.importTransaction(csvFile, accountIban);
    }
}
