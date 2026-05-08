package com.banktracker.model.error;

public record ParsingErrorInfo(
        Long line,
        String message
) {}
