package ch.innovation.ai.tools.demo.dto;

import java.math.BigDecimal;

public record UserBalanceUpdateRequest(
    String transactionId,
    String userId,
    BigDecimal newBalance
) {}
