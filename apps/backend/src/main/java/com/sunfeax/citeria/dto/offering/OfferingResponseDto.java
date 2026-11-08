package com.sunfeax.citeria.dto.offering;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

public record OfferingResponseDto(
    UUID id,
    UUID specialistId,
    String specialistName,
    Long specialistAvatarVersion,
    String name,
    String description,
    BigDecimal priceAmount,
    Integer durationMinutes,
    String currency,
    Boolean isActive,
    Instant createdAt) {
}
