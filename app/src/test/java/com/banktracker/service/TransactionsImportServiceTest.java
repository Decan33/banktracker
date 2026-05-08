package com.banktracker.service;

import com.banktracker.exceptions.CsvImportException;
import com.banktracker.model.ImportStatus;
import com.banktracker.repository.TransactionImportRepository;
import com.banktracker.repository.TransactionStatisticsRepository;
import com.banktracker.util.FileChecksumService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionsImportServiceTest {

    @Mock
    private TransactionStatisticsRepository transactionRepository;

    @Mock
    private TransactionImportRepository transactionImportRepository;

    @Mock
    private FileChecksumService fileChecksumService;

    @InjectMocks
    private TransactionsImportService service;

    @Test
    void shouldRejectDuplicateFile() {
        MockMultipartFile file = new MockMultipartFile(
                "csv",
                "transactions.csv",
                "text/csv",
                "iban,transactionDate,currency,transactionType,amount\n".getBytes()
        );

        when(fileChecksumService.sha256(file)).thenReturn("abc123");
        when(transactionImportRepository.existsByChecksum("abc123")).thenReturn(true);

        assertThatThrownBy(() ->
                service.importTransaction(file)
        ).isInstanceOf(CsvImportException.class)
                .hasMessageContaining("already imported");

        Mockito.verify(transactionRepository, never()).saveAll(any());
    }

    @Test
    void shouldImportValidCsv() {
        String csv = """
                iban,transactionDate,currency,transactionType,amount
                PL94107510605753807963141749,1984-01,PLN,SALARY,5000.00
                PL94107510605753807963141749,1984-01,PLN,GROCERIES,-100.00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "csv",
                "transactions.csv",
                "text/csv",
                csv.getBytes()
        );

        when(fileChecksumService.sha256(file)).thenReturn("abc123");
        when(transactionImportRepository.existsByChecksum("abc123")).thenReturn(false);
        when(transactionImportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.importTransaction(
                file
        );

        Assertions.assertThat(response.status()).isEqualTo(ImportStatus.COMPLETED);
        Assertions.assertThat(response.importedRows()).isEqualTo(2);
        Assertions.assertThat(response.skippedRows()).isZero();

        Mockito.verify(transactionRepository).saveAll(any());
    }

    @Test
    void shouldCompleteWithErrorsWhenCsvHasInvalidRows() {
        String csv = """
                iban,transactionDate,currency,transactionType,amount
                PL94107510605753807963141749,1984-01,PLN,SALARY,5000.00
                PL94107510605753807963141749,wrong,PLN,SALARY,5000.00
                """;

        MockMultipartFile file = new MockMultipartFile(
                "csv",
                "transactions.csv",
                "text/csv",
                csv.getBytes()
        );

        when(fileChecksumService.sha256(file)).thenReturn("abc123");
        when(transactionImportRepository.existsByChecksum("abc123")).thenReturn(false);
        when(transactionImportRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.importTransaction(
                file
        );

        Assertions.assertThat(response.status()).isEqualTo(ImportStatus.COMPLETED_WITH_ERRORS);
        Assertions.assertThat(response.importedRows()).isEqualTo(1);
        Assertions.assertThat(response.skippedRows()).isEqualTo(1);
        Assertions.assertThat(response.errors()).hasSize(1);
    }
}
