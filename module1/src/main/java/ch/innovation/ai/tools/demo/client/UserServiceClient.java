package ch.innovation.ai.tools.demo.client;

import ch.innovation.ai.tools.demo.dto.UserInfoBatchResponse;
import ch.innovation.ai.tools.demo.dto.UserInfoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class UserServiceClient {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceClient.class);
    
    private final RestClient restClient;
    
    public UserServiceClient(@Value("${user.service.url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .build();
    }
    
    public UserInfoBatchResponse getUserInfo(UserInfoRequest request) {
        logger.info("Fetching user info for transaction: {}", request.transactionId());
        
        return restClient.post()
            .uri("/api/users/info")
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .body(UserInfoBatchResponse.class);
    }
    
    public void updateUserBalance(Long userId, BigDecimal newBalance) {
        logger.info("Updating balance for user: {} to {}", userId, newBalance);
        
        Map<String, Object> request = Map.of(
            "userId", userId,
            "balance", newBalance
        );
        
        restClient.put()
            .uri("/api/users/{userId}/balance", userId)
            .contentType(MediaType.APPLICATION_JSON)
            .body(request)
            .retrieve()
            .toBodilessEntity();
    }
}