package ch.innovation.ai.tools.demo.service;

import ch.innovation.ai.tools.demo.dto.*;
import ch.innovation.ai.tools.demo.model.Transaction;
import ch.innovation.ai.tools.demo.model.TransactionStatus;
import ch.innovation.ai.tools.demo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TransactionService transactionService;

    private final String userServiceBaseUrl = "http://localhost:8081/api/v1";

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, restTemplate, userServiceBaseUrl);
    }

    @Test
    void shouldProcessSuccessfulTransaction() {
        // Given
        TransactionRequest request = new TransactionRequest("user_A", "user_B", new BigDecimal("100.00"));
        
        UserBalanceResponse mockResponse = new UserBalanceResponse(
            "some-transaction-id",
            List.of(
                new UserBalanceInfo("user_A", new BigDecimal("500.00"), "USD", "MAIN"),
                new UserBalanceInfo("user_B", new BigDecimal("200.00"), "USD", "MAIN")
            )
        );
        
        when(restTemplate.postForObject(anyString(), any(UserBalanceRequest.class), eq(UserBalanceResponse.class)))
            .thenReturn(mockResponse);

        // When
        TransactionResponse result = transactionService.processTransaction(request);

        // Then
        assertThat(result.transactionId()).isNotNull();
        assertThat(result.senderUserId()).isEqualTo("user_A");
        assertThat(result.receiverUserId()).isEqualTo("user_B");
        assertThat(result.amount()).isEqualTo(new BigDecimal("100.00"));
        assertThat(result.status()).isEqualTo("SUCCESS");
        assertThat(result.senderNewBalance()).isEqualTo(new BigDecimal("400.00"));

        // Verify transaction was saved with SUCCESS status
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        
        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        Transaction finalTransaction = savedTransactions.get(1);
        assertThat(finalTransaction.getStatus()).isEqualTo(TransactionStatus.SUCCESS);
    }

    @Test
    void shouldFailTransactionWhenInsufficientBalance() {
        // Given
        TransactionRequest request = new TransactionRequest("user_A", "user_B", new BigDecimal("600.00"));
        
        UserBalanceResponse mockResponse = new UserBalanceResponse(
            "some-transaction-id",
            List.of(
                new UserBalanceInfo("user_A", new BigDecimal("500.00"), "USD", "MAIN"),
                new UserBalanceInfo("user_B", new BigDecimal("200.00"), "USD", "MAIN")
            )
        );
        
        when(restTemplate.postForObject(anyString(), any(UserBalanceRequest.class), eq(UserBalanceResponse.class)))
            .thenReturn(mockResponse);

        // When
        TransactionResponse result = transactionService.processTransaction(request);

        // Then
        assertThat(result.status()).isEqualTo("FAILED");
        assertThat(result.senderNewBalance()).isEqualTo(new BigDecimal("500.00"));

        // Verify transaction was saved with FAILED status
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        
        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        Transaction finalTransaction = savedTransactions.get(1);
        assertThat(finalTransaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
    }

    @Test
    void shouldFailTransactionWhenUserServiceReturnsInvalidTransactionId() {
        // Given
        TransactionRequest request = new TransactionRequest("user_A", "user_B", new BigDecimal("100.00"));
        
        UserBalanceResponse mockResponse = new UserBalanceResponse(
            "different-transaction-id",
            List.of(
                new UserBalanceInfo("user_A", new BigDecimal("500.00"), "USD", "MAIN"),
                new UserBalanceInfo("user_B", new BigDecimal("200.00"), "USD", "MAIN")
            )
        );
        
        when(restTemplate.postForObject(anyString(), any(UserBalanceRequest.class), eq(UserBalanceResponse.class)))
            .thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> transactionService.processTransaction(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Transaction processing failed");

        // Verify transaction was saved with FAILED status
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        
        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        Transaction finalTransaction = savedTransactions.get(1);
        assertThat(finalTransaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
    }

    @Test
    void shouldFailTransactionWhenSenderNotFound() {
        // Given
        TransactionRequest request = new TransactionRequest("user_A", "user_B", new BigDecimal("100.00"));
        
        UserBalanceResponse mockResponse = new UserBalanceResponse(
            "some-transaction-id",
            List.of(
                new UserBalanceInfo("user_B", new BigDecimal("200.00"), "USD", "MAIN")
            )
        );
        
        when(restTemplate.postForObject(anyString(), any(UserBalanceRequest.class), eq(UserBalanceResponse.class)))
            .thenReturn(mockResponse);

        // When & Then
        assertThatThrownBy(() -> transactionService.processTransaction(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Transaction processing failed");
    }

    @Test
    void shouldGetTransactionHistory() {
        // Given
        String userId = "user_A";
        List<Transaction> mockTransactions = List.of(
            new Transaction("tx1", "user_A", "user_B", new BigDecimal("100.00")),
            new Transaction("tx2", "user_C", "user_A", new BigDecimal("50.00"))
        );
        
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(userId))
            .thenReturn(mockTransactions);

        // When
        List<Transaction> result = transactionService.getTransactionHistory(userId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).isEqualTo(mockTransactions);
        verify(transactionRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }
}
