package ch.innovation.ai.tools.demo.dto;

import java.math.BigDecimal;

public record TransactionResult(String transactionId, String status, BigDecimal newBalance) {}
