package ch.innovation.ai.tools.demo.dto;

import java.math.BigDecimal;

public record UserInfoResponse(
    Long id,
    String name,
    String email,
    BigDecimal balance
) {}