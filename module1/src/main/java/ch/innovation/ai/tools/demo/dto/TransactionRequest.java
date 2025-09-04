package ch.innovation.ai.tools.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransactionRequest(
    @NotBlank(message = "Sender user ID is required")
    String senderUserId,
    
    @NotBlank(message = "Receiver user ID is required") 
    String receiverUserId,
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    BigDecimal amount
) {}
