package ch.innovation.ai.tools.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
    String transactionId,
    String status,
    String message,
    BigDecimal senderBalanceAfter,
    BigDecimal receiverBalanceAfter,
    LocalDateTime timestamp
) {}