package com.sunfeax.citeria.dto.service;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceResponseDto(
    UUID id,
    UUID businessId,
    String name,
    String businessName,
    String description,
    BigDecimal priceAmount,
    Integer durationMinutes,
    String currency,
    Boolean isActive,
    LocalDateTime createdAt) {
}
