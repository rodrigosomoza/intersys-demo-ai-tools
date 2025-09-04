package ch.innovation.ai.tools.demo.dto;

import java.math.BigDecimal;

public record TransactionResponse(
    String transactionId,
    String senderUserId,
    String receiverUserId,
    BigDecimal amount,
    String status,
    BigDecimal senderNewBalance
) {}
