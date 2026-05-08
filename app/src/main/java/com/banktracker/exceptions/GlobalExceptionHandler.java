package com.banktracker.exceptions;

import com.banktracker.model.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CsvImportException.class)
    public ResponseEntity<ApiErrorResponse> handleCsvImportException(
            CsvImportException ex,
            HttpServletRequest request
    ) {
        log.warn("CSV import failed: {}", ex.getMessage());

        return ResponseEntity.badRequest().body(
                new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        "CSV_IMPORT_ERROR",
                        ex.getMessage(),
                        request.getRequestURI()
                )
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error("Unexpected error", ex);

        return ResponseEntity.internalServerError().body(
                new ApiErrorResponse(
                        Instant.now(),
                        500,
                        "INTERNAL_SERVER_ERROR",
                        "Unexpected server error",
                        request.getRequestURI()
                )
        );
    }
}
