package com.banktracker.controller;

import com.banktracker.model.BankingTransactionInfo;
import com.banktracker.model.CategoryStatsResponse;
import com.banktracker.model.MonthlyStatsResponse;
import com.banktracker.service.TransactionsStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
@Slf4j
public class TransactionsStatisticsController {

    private final TransactionsStatisticsService statisticsService;

    @GetMapping("/categories")
    public List<CategoryStatsResponse> statisticsCategories(@RequestParam(required = false) String iban) {
        log.info("Started sorting transactions by categories");
        return statisticsService.getCategories(iban);
    }

    @GetMapping("/month")
    public List<MonthlyStatsResponse> statisticsMonth(@RequestParam(required = false) YearMonth from,
                                                      @RequestParam(required = false) YearMonth to,
                                                      @RequestParam(required = false) String iban
    ) {
        return statisticsService.getStatsByMonth(from, to, iban);
    }

    @GetMapping("/iban")
    public Page<BankingTransactionInfo> statisticsIban(@RequestParam(required = false) String iban,
                                                       @PageableDefault(
                                                               size = 100,
                                                               sort = "iban",
                                                               direction = Sort.Direction.ASC
                                                       ) Pageable pageable
    ) {

        if (iban == null || iban.isBlank()) {
            return statisticsService.getAllTransactions(pageable);
        }

        return statisticsService.getTransactionsWithIban(iban, pageable);
    }
}
