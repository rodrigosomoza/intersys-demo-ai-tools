package ch.innovation.ai.tools.demo.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank String transactionId,
        @NotBlank String fromUserId,
        @NotBlank String toUserId,
        @NotNull @DecimalMin("0.0") BigDecimal amount
) {}
