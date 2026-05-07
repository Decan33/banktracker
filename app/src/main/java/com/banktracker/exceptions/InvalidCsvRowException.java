package com.banktracker.exceptions;

public class InvalidCsvRowException extends RuntimeException {
    public InvalidCsvRowException(String message) {
        super(message);
    }
}
