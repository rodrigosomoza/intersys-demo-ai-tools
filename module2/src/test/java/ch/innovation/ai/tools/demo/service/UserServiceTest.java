package ch.innovation.ai.tools.demo.service;

import ch.innovation.ai.tools.demo.dto.UserInfoBatchResponse;
import ch.innovation.ai.tools.demo.dto.UserInfoRequest;
import ch.innovation.ai.tools.demo.dto.UserInfoResponse;
import ch.innovation.ai.tools.demo.exception.UserNotFoundException;
import ch.innovation.ai.tools.demo.model.User;
import ch.innovation.ai.tools.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void getUserInfo_ShouldReturnUserInfoForValidUserIds() {
        String transactionId = "TXN-12345";
        List<Long> userIds = Arrays.asList(1L, 2L);
        
        User user1 = new User("John Doe", "john@example.com", new BigDecimal("1000.00"));
        user1.setId(1L);
        
        User user2 = new User("Jane Doe", "jane@example.com", new BigDecimal("2000.00"));
        user2.setId(2L);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        
        UserInfoRequest request = new UserInfoRequest(transactionId, userIds);
        
        UserInfoBatchResponse response = userService.getUserInfo(request);
        
        assertThat(response).isNotNull();
        assertThat(response.transactionId()).isEqualTo(transactionId);
        assertThat(response.users()).hasSize(2);
        
        UserInfoResponse userInfo1 = response.users().get(0);
        assertThat(userInfo1.id()).isEqualTo(1L);
        assertThat(userInfo1.name()).isEqualTo("John Doe");
        assertThat(userInfo1.email()).isEqualTo("john@example.com");
        assertThat(userInfo1.balance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        
        UserInfoResponse userInfo2 = response.users().get(1);
        assertThat(userInfo2.id()).isEqualTo(2L);
        assertThat(userInfo2.name()).isEqualTo("Jane Doe");
        assertThat(userInfo2.email()).isEqualTo("jane@example.com");
        assertThat(userInfo2.balance()).isEqualByComparingTo(new BigDecimal("2000.00"));
        
        verify(userRepository, times(2)).findById(anyLong());
    }

    @Test
    void getUserInfo_ShouldThrowExceptionForNonExistentUser() {
        String transactionId = "TXN-12345";
        List<Long> userIds = Arrays.asList(1L, 99L);
        
        User user1 = new User("John Doe", "john@example.com", new BigDecimal("1000.00"));
        user1.setId(1L);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        
        UserInfoRequest request = new UserInfoRequest(transactionId, userIds);
        
        assertThatThrownBy(() -> userService.getUserInfo(request))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User not found with ID: 99");
        
        verify(userRepository).findById(1L);
        verify(userRepository).findById(99L);
    }

    @Test
    void getUserInfo_ShouldHandleEmptyUserIdsList() {
        String transactionId = "TXN-12345";
        List<Long> userIds = Collections.emptyList();
        
        UserInfoRequest request = new UserInfoRequest(transactionId, userIds);
        
        UserInfoBatchResponse response = userService.getUserInfo(request);
        
        assertThat(response).isNotNull();
        assertThat(response.transactionId()).isEqualTo(transactionId);
        assertThat(response.users()).isEmpty();
        
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void getUserInfo_ShouldHandleSingleUserId() {
        String transactionId = "TXN-12345";
        List<Long> userIds = Collections.singletonList(1L);
        
        User user = new User("John Doe", "john@example.com", new BigDecimal("1500.50"));
        user.setId(1L);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        
        UserInfoRequest request = new UserInfoRequest(transactionId, userIds);
        
        UserInfoBatchResponse response = userService.getUserInfo(request);
        
        assertThat(response).isNotNull();
        assertThat(response.transactionId()).isEqualTo(transactionId);
        assertThat(response.users()).hasSize(1);
        
        UserInfoResponse userInfo = response.users().get(0);
        assertThat(userInfo.id()).isEqualTo(1L);
        assertThat(userInfo.balance()).isEqualByComparingTo(new BigDecimal("1500.50"));
        
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserInfo_ShouldHandleDuplicateUserIds() {
        String transactionId = "TXN-12345";
        List<Long> userIds = Arrays.asList(1L, 1L, 2L);
        
        User user1 = new User("John Doe", "john@example.com", new BigDecimal("1000.00"));
        user1.setId(1L);
        
        User user2 = new User("Jane Doe", "jane@example.com", new BigDecimal("2000.00"));
        user2.setId(2L);
        
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
        
        UserInfoRequest request = new UserInfoRequest(transactionId, userIds);
        
        UserInfoBatchResponse response = userService.getUserInfo(request);
        
        assertThat(response).isNotNull();
        assertThat(response.users()).hasSize(3);
        
        verify(userRepository, times(2)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
    }

    @Test
    void updateUserBalance_ShouldUpdateBalanceSuccessfully() {
        Long userId = 1L;
        BigDecimal newBalance = new BigDecimal("500.00");
        
        User user = new User("John Doe", "john@example.com", new BigDecimal("1000.00"));
        user.setId(userId);
        
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        User updatedUser = userService.updateUserBalance(userId, newBalance);
        
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getBalance()).isEqualByComparingTo(newBalance);
        
        verify(userRepository).findById(userId);
        verify(userRepository).save(user);
    }

    @Test
    void updateUserBalance_ShouldThrowExceptionForNonExistentUser() {
        Long userId = 99L;
        BigDecimal newBalance = new BigDecimal("500.00");
        
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        
        assertThatThrownBy(() -> userService.updateUserBalance(userId, newBalance))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("User not found with ID: 99");
        
        verify(userRepository).findById(userId);
        verify(userRepository, never()).save(any());
    }
}