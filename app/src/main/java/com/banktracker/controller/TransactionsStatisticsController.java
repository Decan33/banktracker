package com.banktracker.controller;

import com.banktracker.model.response.CategoryStatsResponse;
import com.banktracker.model.response.MonthlyStatsResponse;
import com.banktracker.model.response.TransactionResponse;
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
@RequestMapping("/api/v1/transactions-statistics")
@RequiredArgsConstructor
@Slf4j
public class TransactionsStatisticsController {

    private final TransactionsStatisticsService statisticsService;

    @GetMapping("/categories")
    public List<CategoryStatsResponse> statisticsCategories() {
        log.info("Started sorting transactions by categories");
        return statisticsService.getCategories();
    }

    @GetMapping("/monthly")
    public List<MonthlyStatsResponse> statisticsMonth(@RequestParam(required = false) YearMonth from,
                                                      @RequestParam(required = false) YearMonth to
    ) {
        return statisticsService.getStatsByMonth(from, to);
    }

    @GetMapping("/iban")
    public Page<TransactionResponse> statisticsIban(@RequestParam(required = false) String iban,
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
