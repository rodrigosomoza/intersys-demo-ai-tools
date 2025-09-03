package ch.innovation.ai.tools.demo.service;

import ch.innovation.ai.tools.demo.dto.UserInfoBatchResponse;
import ch.innovation.ai.tools.demo.dto.UserInfoRequest;
import ch.innovation.ai.tools.demo.dto.UserInfoResponse;
import ch.innovation.ai.tools.demo.exception.UserNotFoundException;
import ch.innovation.ai.tools.demo.model.User;
import ch.innovation.ai.tools.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public UserInfoBatchResponse getUserInfo(UserInfoRequest request) {
        logger.info("Processing user info request for transaction: {}", request.transactionId());
        
        List<UserInfoResponse> userInfoList = new ArrayList<>();
        
        if (request.userIds() == null || request.userIds().isEmpty()) {
            return new UserInfoBatchResponse(request.transactionId(), userInfoList);
        }
        
        for (Long userId : request.userIds()) {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
            
            UserInfoResponse userInfo = new UserInfoResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getBalance()
            );
            
            userInfoList.add(userInfo);
        }
        
        logger.info("Found {} users for transaction: {}", userInfoList.size(), request.transactionId());
        
        return new UserInfoBatchResponse(request.transactionId(), userInfoList);
    }
    
    public User updateUserBalance(Long userId, BigDecimal newBalance) {
        logger.info("Updating balance for user: {} to {}", userId, newBalance);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        user.setBalance(newBalance);
        User updatedUser = userRepository.save(user);
        
        logger.info("Successfully updated balance for user: {}", userId);
        
        return updatedUser;
    }
}