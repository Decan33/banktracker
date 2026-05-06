package com.banktracker.controller;

import com.banktracker.service.TransactionImportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.YearMonth;

@RestController("/transactions")
@RequiredArgsConstructor
public class TransactionsController {

    private final TransactionImportService service;

    @PostMapping("/import")
    public String uploadBankingInformation(@RequestParam("csv") MultipartFile csvFile, YearMonth month, String iban) {

        return null;
    }
}
