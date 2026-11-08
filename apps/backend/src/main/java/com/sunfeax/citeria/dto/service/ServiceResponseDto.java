package com.sunfeax.citeria.dto.service;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

public record ServiceResponseDto(
    UUID id,
    UUID specialistId,
    String specialistName,
    String name,
    String description,
    BigDecimal priceAmount,
    Integer durationMinutes,
    String currency,
    Boolean isActive,
    Instant createdAt) {
}
