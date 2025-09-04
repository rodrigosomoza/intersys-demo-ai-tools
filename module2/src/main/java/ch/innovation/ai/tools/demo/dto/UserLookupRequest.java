package ch.innovation.ai.tools.demo.dto;

import java.util.List;

public record UserLookupRequest(String transactionId, List<String> userIds) {}
