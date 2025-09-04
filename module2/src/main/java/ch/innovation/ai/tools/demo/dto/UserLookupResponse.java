package ch.innovation.ai.tools.demo.dto;

import java.util.List;

public record UserLookupResponse(String transactionId, List<UserInfo> users) {}
