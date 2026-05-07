package com.banktracker.controller;

import com.banktracker.model.CategoryStatsResponse;
import com.banktracker.service.TransactionsStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class TransactionsStatisticsController {

    private final TransactionsStatisticsService statisticsService;

    @PostMapping("/categories")
    public List<CategoryStatsResponse> statisticsCategories(@RequestParam YearMonth from,
                                                            @RequestParam YearMonth to,
                                                            @RequestParam(required = false) String iban
    ) {
        return statisticsService.getCategories(from, to, iban);
    }

    @PostMapping("/month")
    public String statisticsMonth(@RequestParam YearMonth month,
                                  @RequestParam(required = false) String iban
    ) {
        return statisticsService.getStatsByMonth(month, iban);
    }

    @PostMapping("/iban")
    public String statisticsIban(@RequestParam YearMonth month,
                                 @RequestParam String iban
    ) {

        return statisticsService.getIbans();
    }
}
