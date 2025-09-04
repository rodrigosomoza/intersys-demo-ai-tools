package ch.innovation.ai.tools.demo.dto;

public record UserBalanceUpdateResponse(
    String transactionId,
    String userId,
    String status,
    String message
) {}
