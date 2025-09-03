package ch.innovation.ai.tools.demo.dto;

import java.util.List;

public record UserInfoBatchResponse(
    String transactionId,
    List<UserInfoResponse> users
) {}