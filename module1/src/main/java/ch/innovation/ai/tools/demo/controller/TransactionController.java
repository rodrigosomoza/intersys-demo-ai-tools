package ch.innovation.ai.tools.demo.controller;

import ch.innovation.ai.tools.demo.dto.TransactionRequest;
import ch.innovation.ai.tools.demo.dto.TransactionResponse;
import ch.innovation.ai.tools.demo.exception.InsufficientFundsException;
import ch.innovation.ai.tools.demo.exception.TransactionException;
import ch.innovation.ai.tools.demo.service.TransactionService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction Management", description = "Financial transaction processing APIs")
public class TransactionController {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);
    
    private final TransactionService transactionService;
    
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    
    @PostMapping
    @Operation(summary = "Process a transaction", description = "Process a financial transaction between two users")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction processed successfully",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request or insufficient funds"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TransactionResponse> processTransaction(@Valid @RequestBody TransactionRequest request) {
        logger.info("Received transaction request from {} to {} for amount {}", 
            request.senderId(), request.receiverId(), request.amount());
        
        try {
            TransactionResponse response = transactionService.processTransaction(request);
            return ResponseEntity.ok(response);
        } catch (InsufficientFundsException e) {
            logger.error("Transaction failed due to insufficient funds: {}", e.getMessage());
            throw e;
        } catch (TransactionException e) {
            logger.error("Transaction failed: {}", e.getMessage());
            throw e;
        }
    }
    
    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientFunds(InsufficientFundsException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "error", e.getMessage(),
                "status", "INSUFFICIENT_FUNDS"
            ));
    }
    
    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<Map<String, String>> handleTransactionException(TransactionException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "error", e.getMessage(),
                "status", "FAILED"
            ));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception e) {
        logger.error("Unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "error", "An unexpected error occurred",
                "status", "ERROR"
            ));
    }
}