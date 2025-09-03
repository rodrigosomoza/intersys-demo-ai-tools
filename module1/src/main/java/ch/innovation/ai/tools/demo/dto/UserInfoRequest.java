package ch.innovation.ai.tools.demo.dto;

import java.util.List;

public record UserInfoRequest(
    String transactionId,
    List<Long> userIds
) {}