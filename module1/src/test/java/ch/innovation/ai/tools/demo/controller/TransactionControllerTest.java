package ch.innovation.ai.tools.demo.controller;

import ch.innovation.ai.tools.demo.dto.TransactionRequest;
import ch.innovation.ai.tools.demo.dto.TransactionResponse;
import ch.innovation.ai.tools.demo.exception.InsufficientFundsException;
import ch.innovation.ai.tools.demo.exception.TransactionException;
import ch.innovation.ai.tools.demo.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @Test
    void processTransaction_ShouldReturnSuccessResponse() throws Exception {
        TransactionRequest request = new TransactionRequest(1L, 2L, new BigDecimal("500.00"));
        
        TransactionResponse response = new TransactionResponse(
            "TXN-123456",
            "SUCCESS",
            "Transaction completed successfully",
            new BigDecimal("500.00"),
            new BigDecimal("2500.00"),
            LocalDateTime.now()
        );
        
        when(transactionService.processTransaction(any(TransactionRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value("TXN-123456"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Transaction completed successfully"))
                .andExpect(jsonPath("$.senderBalanceAfter").value(500.00))
                .andExpect(jsonPath("$.receiverBalanceAfter").value(2500.00));
        
        verify(transactionService, times(1)).processTransaction(any(TransactionRequest.class));
    }

    @Test
    void processTransaction_ShouldReturnBadRequestForNullSenderId() throws Exception {
        TransactionRequest request = new TransactionRequest(null, 2L, new BigDecimal("500.00"));
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        
        verify(transactionService, never()).processTransaction(any());
    }

    @Test
    void processTransaction_ShouldReturnBadRequestForNullReceiverId() throws Exception {
        TransactionRequest request = new TransactionRequest(1L, null, new BigDecimal("500.00"));
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        
        verify(transactionService, never()).processTransaction(any());
    }

    @Test
    void processTransaction_ShouldReturnBadRequestForNullAmount() throws Exception {
        TransactionRequest request = new TransactionRequest(1L, 2L, null);
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        
        verify(transactionService, never()).processTransaction(any());
    }

    @Test
    void processTransaction_ShouldReturnBadRequestForNegativeAmount() throws Exception {
        TransactionRequest request = new TransactionRequest(1L, 2L, new BigDecimal("-100.00"));
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        
        verify(transactionService, never()).processTransaction(any());
    }

    @Test
    void processTransaction_ShouldReturnBadRequestForZeroAmount() throws Exception {
        TransactionRequest request = new TransactionRequest(1L, 2L, BigDecimal.ZERO);
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        
        verify(transactionService, never()).processTransaction(any());
    }

    @Test
    void processTransaction_ShouldHandleInsufficientFunds() throws Exception {
        TransactionRequest request = new TransactionRequest(1L, 2L, new BigDecimal("1000.00"));
        
        when(transactionService.processTransaction(any(TransactionRequest.class)))
            .thenThrow(new InsufficientFundsException("Insufficient funds for transaction"));
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Insufficient funds for transaction"))
                .andExpect(jsonPath("$.status").value("INSUFFICIENT_FUNDS"));
        
        verify(transactionService, times(1)).processTransaction(any(TransactionRequest.class));
    }

    @Test
    void processTransaction_ShouldHandleGeneralTransactionException() throws Exception {
        TransactionRequest request = new TransactionRequest(1L, 99L, new BigDecimal("500.00"));
        
        when(transactionService.processTransaction(any(TransactionRequest.class)))
            .thenThrow(new TransactionException("User not found"));
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("User not found"))
                .andExpect(jsonPath("$.status").value("FAILED"));
        
        verify(transactionService, times(1)).processTransaction(any(TransactionRequest.class));
    }

    @Test
    void processTransaction_ShouldHandleUnexpectedError() throws Exception {
        TransactionRequest request = new TransactionRequest(1L, 2L, new BigDecimal("500.00"));
        
        when(transactionService.processTransaction(any(TransactionRequest.class)))
            .thenThrow(new RuntimeException("Unexpected error"));
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("An unexpected error occurred"))
                .andExpect(jsonPath("$.status").value("ERROR"));
        
        verify(transactionService, times(1)).processTransaction(any(TransactionRequest.class));
    }

    @Test
    void processTransaction_ShouldHandleLargeAmount() throws Exception {
        TransactionRequest request = new TransactionRequest(1L, 2L, new BigDecimal("999999999.99"));
        
        TransactionResponse response = new TransactionResponse(
            "TXN-123456",
            "SUCCESS",
            "Transaction completed successfully",
            new BigDecimal("1000000.01"),
            new BigDecimal("1999999999.99"),
            LocalDateTime.now()
        );
        
        when(transactionService.processTransaction(any(TransactionRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        
        verify(transactionService, times(1)).processTransaction(any(TransactionRequest.class));
    }

    @Test
    void processTransaction_ShouldHandleSmallAmount() throws Exception {
        TransactionRequest request = new TransactionRequest(1L, 2L, new BigDecimal("0.01"));
        
        TransactionResponse response = new TransactionResponse(
            "TXN-123456",
            "SUCCESS",
            "Transaction completed successfully",
            new BigDecimal("999.99"),
            new BigDecimal("2000.01"),
            LocalDateTime.now()
        );
        
        when(transactionService.processTransaction(any(TransactionRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));
        
        verify(transactionService, times(1)).processTransaction(any(TransactionRequest.class));
    }
}