package com.banktracker.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController("/statistics")
@RequiredArgsConstructor
public class TransactionsStatisticsController {

    @PostMapping("/categories")
    public String statisticsCategories(@RequestParam YearMonth from,
                                       @RequestParam YearMonth to,
                                       @RequestParam(required = false) String iban
    ) {

        return null;
    }

    @PostMapping("/month")
    public String statisticsMonth(@RequestParam YearMonth month,
                                  @RequestParam(required = false) String iban
    ) {
        return null;
    }

    @PostMapping("/iban")
    public String statisticsIban(@RequestParam YearMonth month,
                                 @RequestParam String iban
    ) {

        return null;
    }
}
