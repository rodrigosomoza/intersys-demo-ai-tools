package ch.innovation.ai.tools.demo.dto;

import java.util.List;

public record UserBalanceRequest(
    String transactionId,
    List<String> userIds
) {}
