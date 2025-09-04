package ch.innovation.ai.tools.demo.dto;

import java.math.BigDecimal;

public record UserBalanceInfo(
    String userId,
    BigDecimal balance,
    String currency,
    String accountType
) {}
