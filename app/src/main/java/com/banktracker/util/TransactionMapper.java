package com.banktracker.util;

import com.banktracker.model.TransactionDocument;
import com.banktracker.model.TransactionResponse;

public class TransactionMapper {

    public static TransactionResponse toResponse(
            TransactionDocument transactionInfo
    ) {
        return new TransactionResponse(
                transactionInfo.iban(),
                transactionInfo.transactionDate(),
                transactionInfo.currency(),
                transactionInfo.transactionType(),
                transactionInfo.amount()
        );
    }
}
