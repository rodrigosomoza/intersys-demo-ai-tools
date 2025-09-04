package ch.innovation.ai.tools.demo.dto;

import java.math.BigDecimal;
import java.util.List;

public class UserLookupDtos {
    public record UserInfo(String userId, String name, BigDecimal balance) {}
    public record UserLookupRequest(String transactionId, List<String> userIds) {}
    public record UserLookupResponse(String transactionId, List<UserInfo> users) {}
}
