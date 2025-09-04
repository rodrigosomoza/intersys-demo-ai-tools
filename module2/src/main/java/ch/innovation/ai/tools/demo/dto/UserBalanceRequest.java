package ch.innovation.ai.tools.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserBalanceRequest(
    @NotBlank(message = "Transaction ID is required")
    String transactionId,
    
    @NotNull(message = "User IDs list is required")
    List<String> userIds
) {}
