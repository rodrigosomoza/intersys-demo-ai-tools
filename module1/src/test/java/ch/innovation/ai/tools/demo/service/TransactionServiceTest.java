package ch.innovation.ai.tools.demo.service;

import ch.innovation.ai.tools.demo.client.UserServiceClient;
import ch.innovation.ai.tools.demo.dto.*;
import ch.innovation.ai.tools.demo.exception.InsufficientFundsException;
import ch.innovation.ai.tools.demo.exception.TransactionException;
import ch.innovation.ai.tools.demo.exception.TransactionIdMismatchException;
import ch.innovation.ai.tools.demo.model.Transaction;
import ch.innovation.ai.tools.demo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserServiceClient userServiceClient;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, userServiceClient);
    }

    @Test
    void processTransaction_ShouldSucceedWithSufficientBalance() {
        Long senderId = 1L;
        Long receiverId = 2L;
        BigDecimal amount = new BigDecimal("500.00");
        
        TransactionRequest request = new TransactionRequest(senderId, receiverId, amount);
        
        List<UserInfoResponse> users = Arrays.asList(
            new UserInfoResponse(1L, "John Doe", "john@example.com", new BigDecimal("1000.00")),
            new UserInfoResponse(2L, "Jane Doe", "jane@example.com", new BigDecimal("2000.00"))
        );
        
        when(userServiceClient.getUserInfo(any(UserInfoRequest.class))).thenAnswer(invocation -> {
            UserInfoRequest userInfoRequest = invocation.getArgument(0);
            return new UserInfoBatchResponse(userInfoRequest.transactionId(), users);
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        
        TransactionResponse response = transactionService.processTransaction(request);
        
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.senderBalanceAfter()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.receiverBalanceAfter()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(response.transactionId()).startsWith("TXN-");
        
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        
        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        assertThat(savedTransactions).hasSize(2);
        
        Transaction firstSave = savedTransactions.get(0);
        assertThat(firstSave.getStatus()).isIn(Transaction.TransactionStatus.PENDING, Transaction.TransactionStatus.SUCCESS);
        
        Transaction secondSave = savedTransactions.get(1);
        assertThat(secondSave.getStatus()).isEqualTo(Transaction.TransactionStatus.SUCCESS);
        assertThat(secondSave.getSenderBalanceAfter()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(secondSave.getReceiverBalanceAfter()).isEqualByComparingTo(new BigDecimal("2500.00"));
        
        verify(userServiceClient).updateUserBalance(senderId, new BigDecimal("500.00"));
        verify(userServiceClient).updateUserBalance(receiverId, new BigDecimal("2500.00"));
    }

    @Test
    void processTransaction_ShouldFailWithInsufficientBalance() {
        Long senderId = 1L;
        Long receiverId = 2L;
        BigDecimal amount = new BigDecimal("1500.00");
        
        TransactionRequest request = new TransactionRequest(senderId, receiverId, amount);
        
        List<UserInfoResponse> users = Arrays.asList(
            new UserInfoResponse(1L, "John Doe", "john@example.com", new BigDecimal("1000.00")),
            new UserInfoResponse(2L, "Jane Doe", "jane@example.com", new BigDecimal("2000.00"))
        );
        
        when(userServiceClient.getUserInfo(any(UserInfoRequest.class))).thenAnswer(invocation -> {
            UserInfoRequest userInfoRequest = invocation.getArgument(0);
            return new UserInfoBatchResponse(userInfoRequest.transactionId(), users);
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        
        assertThatThrownBy(() -> transactionService.processTransaction(request))
            .isInstanceOf(InsufficientFundsException.class)
            .hasMessageContaining("Insufficient funds");
        
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        
        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        Transaction lastSave = savedTransactions.get(1);
        assertThat(lastSave.getStatus()).isEqualTo(Transaction.TransactionStatus.INSUFFICIENT_FUNDS);
        
        verify(userServiceClient, never()).updateUserBalance(anyLong(), any());
    }

    @Test
    void processTransaction_ShouldFailWhenSenderNotFound() {
        Long senderId = 99L;
        Long receiverId = 2L;
        BigDecimal amount = new BigDecimal("500.00");
        
        TransactionRequest request = new TransactionRequest(senderId, receiverId, amount);
        
        List<UserInfoResponse> users = Arrays.asList(
            new UserInfoResponse(2L, "Jane Doe", "jane@example.com", new BigDecimal("2000.00"))
        );
        
        when(userServiceClient.getUserInfo(any(UserInfoRequest.class))).thenAnswer(invocation -> {
            UserInfoRequest userInfoRequest = invocation.getArgument(0);
            return new UserInfoBatchResponse(userInfoRequest.transactionId(), users);
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        
        assertThatThrownBy(() -> transactionService.processTransaction(request))
            .isInstanceOf(TransactionException.class)
            .hasMessageContaining("User not found");
        
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        
        List<Transaction> savedTransactions = transactionCaptor.getAllValues();
        Transaction lastSave = savedTransactions.get(1);
        assertThat(lastSave.getStatus()).isEqualTo(Transaction.TransactionStatus.USER_NOT_FOUND);
        
        verify(userServiceClient, never()).updateUserBalance(anyLong(), any());
    }

    @Test
    void processTransaction_ShouldFailWhenReceiverNotFound() {
        Long senderId = 1L;
        Long receiverId = 99L;
        BigDecimal amount = new BigDecimal("500.00");
        
        TransactionRequest request = new TransactionRequest(senderId, receiverId, amount);
        
        List<UserInfoResponse> users = Arrays.asList(
            new UserInfoResponse(1L, "John Doe", "john@example.com", new BigDecimal("1000.00"))
        );
        
        when(userServiceClient.getUserInfo(any(UserInfoRequest.class))).thenAnswer(invocation -> {
            UserInfoRequest userInfoRequest = invocation.getArgument(0);
            return new UserInfoBatchResponse(userInfoRequest.transactionId(), users);
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        
        assertThatThrownBy(() -> transactionService.processTransaction(request))
            .isInstanceOf(TransactionException.class)
            .hasMessageContaining("User not found");
        
        verify(userServiceClient, never()).updateUserBalance(anyLong(), any());
    }

    @Test
    void processTransaction_ShouldFailOnTransactionIdMismatch() {
        Long senderId = 1L;
        Long receiverId = 2L;
        BigDecimal amount = new BigDecimal("500.00");
        
        TransactionRequest request = new TransactionRequest(senderId, receiverId, amount);
        
        List<UserInfoResponse> users = Arrays.asList(
            new UserInfoResponse(1L, "John Doe", "john@example.com", new BigDecimal("1000.00")),
            new UserInfoResponse(2L, "Jane Doe", "jane@example.com", new BigDecimal("2000.00"))
        );
        
        UserInfoBatchResponse userInfoResponse = new UserInfoBatchResponse("DIFFERENT-TXN-ID", users);
        
        when(userServiceClient.getUserInfo(any(UserInfoRequest.class))).thenReturn(userInfoResponse);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        
        assertThatThrownBy(() -> transactionService.processTransaction(request))
            .isInstanceOf(TransactionIdMismatchException.class)
            .hasMessageContaining("Transaction ID mismatch");
        
        verify(userServiceClient, never()).updateUserBalance(anyLong(), any());
    }

    @Test
    void processTransaction_ShouldHandleSameUserTransaction() {
        Long userId = 1L;
        BigDecimal amount = new BigDecimal("500.00");
        
        TransactionRequest request = new TransactionRequest(userId, userId, amount);
        
        assertThatThrownBy(() -> transactionService.processTransaction(request))
            .isInstanceOf(TransactionException.class)
            .hasMessageContaining("Cannot transfer to the same account");
        
        verify(userServiceClient, never()).getUserInfo(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processTransaction_ShouldHandleZeroAmount() {
        Long senderId = 1L;
        Long receiverId = 2L;
        BigDecimal amount = BigDecimal.ZERO;
        
        TransactionRequest request = new TransactionRequest(senderId, receiverId, amount);
        
        assertThatThrownBy(() -> transactionService.processTransaction(request))
            .isInstanceOf(TransactionException.class)
            .hasMessageContaining("Amount must be greater than zero");
        
        verify(userServiceClient, never()).getUserInfo(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processTransaction_ShouldHandleExactBalance() {
        Long senderId = 1L;
        Long receiverId = 2L;
        BigDecimal amount = new BigDecimal("1000.00");
        
        TransactionRequest request = new TransactionRequest(senderId, receiverId, amount);
        
        List<UserInfoResponse> users = Arrays.asList(
            new UserInfoResponse(1L, "John Doe", "john@example.com", new BigDecimal("1000.00")),
            new UserInfoResponse(2L, "Jane Doe", "jane@example.com", new BigDecimal("2000.00"))
        );
        
        when(userServiceClient.getUserInfo(any(UserInfoRequest.class))).thenAnswer(invocation -> {
            UserInfoRequest userInfoRequest = invocation.getArgument(0);
            return new UserInfoBatchResponse(userInfoRequest.transactionId(), users);
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction t = invocation.getArgument(0);
            t.setId(1L);
            return t;
        });
        
        TransactionResponse response = transactionService.processTransaction(request);
        
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.senderBalanceAfter()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.receiverBalanceAfter()).isEqualByComparingTo(new BigDecimal("3000.00"));
        
        verify(userServiceClient).updateUserBalance(senderId, new BigDecimal("0.00"));
        verify(userServiceClient).updateUserBalance(receiverId, new BigDecimal("3000.00"));
    }
}