package ch.innovation.ai.tools.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record TransactionRequest(
    @NotNull(message = "Sender ID is required")
    Long senderId,
    
    @NotNull(message = "Receiver ID is required")
    Long receiverId,
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    BigDecimal amount
) {}