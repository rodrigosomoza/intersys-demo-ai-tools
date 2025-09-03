package ch.innovation.ai.tools.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserInfoRequest(
    @NotNull(message = "Transaction ID is required")
    String transactionId,
    
    @NotNull(message = "User IDs are required")
    @NotEmpty(message = "User IDs list cannot be empty")
    List<Long> userIds
) {}