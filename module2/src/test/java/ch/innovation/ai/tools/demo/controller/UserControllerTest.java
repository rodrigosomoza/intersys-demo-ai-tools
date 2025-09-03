package ch.innovation.ai.tools.demo.controller;

import ch.innovation.ai.tools.demo.dto.UserInfoBatchResponse;
import ch.innovation.ai.tools.demo.dto.UserInfoRequest;
import ch.innovation.ai.tools.demo.dto.UserInfoResponse;
import ch.innovation.ai.tools.demo.exception.UserNotFoundException;
import ch.innovation.ai.tools.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void getUserInfo_ShouldReturnUserInfoSuccessfully() throws Exception {
        String transactionId = "TXN-12345";
        List<Long> userIds = Arrays.asList(1L, 2L);
        UserInfoRequest request = new UserInfoRequest(transactionId, userIds);
        
        List<UserInfoResponse> users = Arrays.asList(
            new UserInfoResponse(1L, "John Doe", "john@example.com", new BigDecimal("1000.00")),
            new UserInfoResponse(2L, "Jane Doe", "jane@example.com", new BigDecimal("2000.00"))
        );
        
        UserInfoBatchResponse response = new UserInfoBatchResponse(transactionId, users);
        
        when(userService.getUserInfo(any(UserInfoRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/users/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users.length()").value(2))
                .andExpect(jsonPath("$.users[0].id").value(1))
                .andExpect(jsonPath("$.users[0].name").value("John Doe"))
                .andExpect(jsonPath("$.users[0].balance").value(1000.00))
                .andExpect(jsonPath("$.users[1].id").value(2))
                .andExpect(jsonPath("$.users[1].name").value("Jane Doe"))
                .andExpect(jsonPath("$.users[1].balance").value(2000.00));
        
        verify(userService, times(1)).getUserInfo(any(UserInfoRequest.class));
    }

    @Test
    void getUserInfo_ShouldReturnBadRequestForNullTransactionId() throws Exception {
        UserInfoRequest request = new UserInfoRequest(null, Arrays.asList(1L, 2L));
        
        mockMvc.perform(post("/api/users/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        
        verify(userService, never()).getUserInfo(any());
    }

    @Test
    void getUserInfo_ShouldReturnBadRequestForNullUserIds() throws Exception {
        UserInfoRequest request = new UserInfoRequest("TXN-12345", null);
        
        mockMvc.perform(post("/api/users/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        
        verify(userService, never()).getUserInfo(any());
    }

    @Test
    void getUserInfo_ShouldReturnBadRequestForEmptyUserIds() throws Exception {
        UserInfoRequest request = new UserInfoRequest("TXN-12345", Collections.emptyList());
        
        mockMvc.perform(post("/api/users/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
        
        verify(userService, never()).getUserInfo(any());
    }

    @Test
    void getUserInfo_ShouldReturnNotFoundForNonExistentUser() throws Exception {
        String transactionId = "TXN-12345";
        List<Long> userIds = Arrays.asList(1L, 99L);
        UserInfoRequest request = new UserInfoRequest(transactionId, userIds);
        
        when(userService.getUserInfo(any(UserInfoRequest.class)))
            .thenThrow(new UserNotFoundException("User not found with ID: 99"));
        
        mockMvc.perform(post("/api/users/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with ID: 99"));
        
        verify(userService, times(1)).getUserInfo(any(UserInfoRequest.class));
    }

    @Test
    void getUserInfo_ShouldHandleLargeUserIdsList() throws Exception {
        String transactionId = "TXN-12345";
        List<Long> userIds = Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L);
        UserInfoRequest request = new UserInfoRequest(transactionId, userIds);
        
        List<UserInfoResponse> users = userIds.stream()
            .map(id -> new UserInfoResponse(id, "User " + id, "user" + id + "@example.com", 
                new BigDecimal(id * 100)))
            .toList();
        
        UserInfoBatchResponse response = new UserInfoBatchResponse(transactionId, users);
        
        when(userService.getUserInfo(any(UserInfoRequest.class))).thenReturn(response);
        
        mockMvc.perform(post("/api/users/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value(transactionId))
                .andExpect(jsonPath("$.users.length()").value(10));
        
        verify(userService, times(1)).getUserInfo(any(UserInfoRequest.class));
    }

    @Test
    void getUserInfo_ShouldReturnBadRequestForInvalidJson() throws Exception {
        String invalidJson = "{\"transactionId\": \"TXN-12345\", \"userIds\": [1, \"invalid\"]}";
        
        mockMvc.perform(post("/api/users/info")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().is4xxClientError());
        
        verify(userService, never()).getUserInfo(any());
    }
}