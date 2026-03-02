package com.sunfeax.citeria.dto.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServiceResponseDto(
    Long id,
    String name,
    String businessName,
    String description,
    BigDecimal priceAmount,
    Integer durationMinutes,
    Boolean isActive,
    LocalDateTime createdAt) {
}
