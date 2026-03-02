package com.sunfeax.citeria.dto.offering;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OfferingResponseDto(
    Long id,
    Long businessId,
    String businessName,
    Long specialistId,
    String specialistName,
    Long serviceId,
    String serviceName,
    BigDecimal priceAmount,
    Integer durationMinutes,
    Boolean isActive,
    LocalDateTime createdAt) {
}
