package ch.innovation.ai.tools.demo.integration;

import ch.innovation.ai.tools.demo.client.UserServiceClient;
import ch.innovation.ai.tools.demo.dto.TransactionRequest;
import ch.innovation.ai.tools.demo.dto.TransactionResponse;
import ch.innovation.ai.tools.demo.dto.UserInfoBatchResponse;
import ch.innovation.ai.tools.demo.dto.UserInfoRequest;
import ch.innovation.ai.tools.demo.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class TransactionIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @MockBean
    private UserServiceClient userServiceClient;

    @Test
    void should_ProcessSuccessfulTransaction() {
        // Given
        TransactionRequest request = new TransactionRequest(1L, 2L, new BigDecimal("500.00"));
        
        // Mock the user service response
        when(userServiceClient.getUserInfo(any(UserInfoRequest.class))).thenAnswer(invocation -> {
            UserInfoRequest userInfoRequest = invocation.getArgument(0);
            return new UserInfoBatchResponse(userInfoRequest.transactionId(), Arrays.asList(
                new ch.innovation.ai.tools.demo.dto.UserInfoResponse(1L, "John Doe", "john@example.com", new BigDecimal("1000.00")),
                new ch.innovation.ai.tools.demo.dto.UserInfoResponse(2L, "Jane Doe", "jane@example.com", new BigDecimal("2000.00"))
            ));
        });
        
        // When
        TransactionResponse response = transactionService.processTransaction(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.senderBalanceAfter()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(response.receiverBalanceAfter()).isEqualByComparingTo(new BigDecimal("2500.00"));
        assertThat(response.transactionId()).startsWith("TXN-");
        
        // Verify user service interactions
        verify(userServiceClient, times(1)).getUserInfo(any(UserInfoRequest.class));
        verify(userServiceClient, times(1)).updateUserBalance(1L, new BigDecimal("500.00"));
        verify(userServiceClient, times(1)).updateUserBalance(2L, new BigDecimal("2500.00"));
    }
}