package ch.innovation.ai.tools.demo.service;

import ch.innovation.ai.tools.demo.dto.UserBalanceInfo;
import ch.innovation.ai.tools.demo.dto.UserBalanceRequest;
import ch.innovation.ai.tools.demo.dto.UserBalanceResponse;
import ch.innovation.ai.tools.demo.model.UserAccount;
import ch.innovation.ai.tools.demo.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private UserAccountService userAccountService;

    @Test
    void shouldGetUserBalancesSuccessfully() {
        // Given
        String transactionId = "tx-123";
        List<String> userIds = List.of("user_A", "user_B");
        
        List<UserAccount> mockAccounts = List.of(
            new UserAccount("acc_001", "user_A", new BigDecimal("1000.00"), "USD", "MAIN"),
            new UserAccount("acc_002", "user_B", new BigDecimal("500.00"), "USD", "MAIN")
        );
        
        when(userAccountRepository.findByUserIdIn(userIds)).thenReturn(mockAccounts);
        
        UserBalanceRequest request = new UserBalanceRequest(transactionId, userIds);

        // When
        UserBalanceResponse result = userAccountService.getUserBalances(request);

        // Then
        assertThat(result.transactionId()).isEqualTo(transactionId);
        assertThat(result.users()).hasSize(2);
        
        UserBalanceInfo userA = result.users().get(0);
        assertThat(userA.userId()).isEqualTo("user_A");
        assertThat(userA.balance()).isEqualTo(new BigDecimal("1000.00"));
        assertThat(userA.currency()).isEqualTo("USD");
        assertThat(userA.accountType()).isEqualTo("MAIN");
        
        UserBalanceInfo userB = result.users().get(1);
        assertThat(userB.userId()).isEqualTo("user_B");
        assertThat(userB.balance()).isEqualTo(new BigDecimal("500.00"));
        
        verify(userAccountRepository).findByUserIdIn(userIds);
    }

    @Test
    void shouldUpdateUserBalanceSuccessfully() {
        // Given
        String userId = "user_A";
        BigDecimal newBalance = new BigDecimal("750.00");
        
        UserAccount mockAccount = new UserAccount("acc_001", "user_A", new BigDecimal("1000.00"), "USD", "MAIN");
        when(userAccountRepository.findByUserId(userId)).thenReturn(Optional.of(mockAccount));
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(mockAccount);

        // When
        userAccountService.updateUserBalance(userId, newBalance);

        // Then
        verify(userAccountRepository).findByUserId(userId);
        verify(userAccountRepository).save(mockAccount);
        assertThat(mockAccount.getBalance()).isEqualTo(newBalance);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFoundForBalanceUpdate() {
        // Given
        String userId = "non_existent_user";
        BigDecimal newBalance = new BigDecimal("750.00");
        
        when(userAccountRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userAccountService.updateUserBalance(userId, newBalance))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User account not found: " + userId);
        
        verify(userAccountRepository).findByUserId(userId);
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void shouldGetUserAccountSuccessfully() {
        // Given
        String userId = "user_A";
        UserAccount mockAccount = new UserAccount("acc_001", "user_A", new BigDecimal("1000.00"), "USD", "MAIN");
        when(userAccountRepository.findByUserId(userId)).thenReturn(Optional.of(mockAccount));

        // When
        UserAccount result = userAccountService.getUserAccount(userId);

        // Then
        assertThat(result).isEqualTo(mockAccount);
        verify(userAccountRepository).findByUserId(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserAccountNotFound() {
        // Given
        String userId = "non_existent_user";
        when(userAccountRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userAccountService.getUserAccount(userId))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User account not found: " + userId);
        
        verify(userAccountRepository).findByUserId(userId);
    }
}
