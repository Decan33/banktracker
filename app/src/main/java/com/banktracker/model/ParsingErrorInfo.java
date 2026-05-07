package com.banktracker.model;

public record ParsingErrorInfo(Long line, String column, String message) {
}
