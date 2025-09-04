package ch.innovation.ai.tools.demo.dto;

import java.util.List;

public record UserBalanceResponse(
    String transactionId,
    List<UserBalanceInfo> users
) {}
