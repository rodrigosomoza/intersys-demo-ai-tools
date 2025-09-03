package ch.innovation.ai.tools.demo.controller;

import ch.innovation.ai.tools.demo.dto.UserInfoBatchResponse;
import ch.innovation.ai.tools.demo.dto.UserInfoRequest;
import ch.innovation.ai.tools.demo.exception.UserNotFoundException;
import ch.innovation.ai.tools.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User information and balance management APIs")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    @PostMapping("/info")
    @Operation(summary = "Get user information", description = "Retrieve user information including balance for multiple users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user information",
            content = @Content(schema = @Schema(implementation = UserInfoBatchResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "One or more users not found")
    })
    public ResponseEntity<UserInfoBatchResponse> getUserInfo(@Valid @RequestBody UserInfoRequest request) {
        logger.info("Received user info request for transaction: {}", request.transactionId());
        
        try {
            UserInfoBatchResponse response = userService.getUserInfo(request);
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            throw e;
        }
    }
    
    @PutMapping("/{userId}/balance")
    @Operation(summary = "Update user balance", description = "Update the balance of a specific user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Balance updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> updateUserBalance(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        logger.info("Updating balance for user: {}", userId);
        
        try {
            BigDecimal newBalance = new BigDecimal(request.get("balance").toString());
            userService.updateUserBalance(userId, newBalance);
            return ResponseEntity.ok().build();
        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            throw e;
        }
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        logger.error("Unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of("error", "An unexpected error occurred"));
    }
}