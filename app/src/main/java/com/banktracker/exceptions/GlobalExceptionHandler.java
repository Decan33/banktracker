package com.banktracker.exceptions;

import com.banktracker.model.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.time.format.DateTimeParseException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

        @ExceptionHandler(CsvImportException.class)
        public ResponseEntity<ApiErrorResponse> handleCsvImportException(
                        CsvImportException ex,
                        HttpServletRequest request) {
                log.warn("CSV import failed: {}", ex.getMessage());

                return ResponseEntity.badRequest().body(
                                new ApiErrorResponse(
                                                Instant.now(),
                                                HttpStatus.BAD_REQUEST.value(),
                                                "CSV_IMPORT_ERROR",
                                                ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(DateTimeParseException.class)
        public ResponseEntity<ApiErrorResponse> handleDateTimeParseException(
                        DateTimeParseException ex,
                        HttpServletRequest request) {
                log.warn("Date/time parse failed: {}", ex.getMessage());

                return ResponseEntity.badRequest().body(
                                new ApiErrorResponse(
                                                Instant.now(),
                                                HttpStatus.BAD_REQUEST.value(),
                                                "DATE_TIME_PARSE_ERROR",
                                                ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(DateConflictException.class)
        public ResponseEntity<ApiErrorResponse> handleDateConflictException(
                        DateConflictException ex,
                        HttpServletRequest request) {
                log.warn("Invalid date range: {}", ex.getMessage());

                return ResponseEntity.badRequest().body(
                                new ApiErrorResponse(
                                                Instant.now(),
                                                HttpStatus.BAD_REQUEST.value(),
                                                "DATE_CONFLICT_ERROR",
                                                ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiErrorResponse> handleConversionFailedException(
                        ConversionFailedException ex,
                        HttpServletRequest request) {
                log.warn("Invalid conversion: {}", ex.getMessage());

                return ResponseEntity.badRequest().body(
                                new ApiErrorResponse(
                                                Instant.now(),
                                                HttpStatus.BAD_REQUEST.value(),
                                                "CONVERSION_ERROR",
                                                ex.getMessage(),
                                                request.getRequestURI()));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleUnexpected(
                        Exception ex,
                        HttpServletRequest request) {
                log.error("Unexpected error", ex);

                return ResponseEntity.internalServerError().body(
                                new ApiErrorResponse(
                                                Instant.now(),
                                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                                "INTERNAL_SERVER_ERROR",
                                                "Unexpected server error",
                                                request.getRequestURI()));
        }
}
