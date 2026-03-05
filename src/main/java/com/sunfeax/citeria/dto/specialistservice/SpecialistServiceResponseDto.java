package com.sunfeax.citeria.dto.specialistservice;

import java.time.LocalDateTime;

public record SpecialistServiceResponseDto(
    Long id,
    Long businessId,
    String businessName,
    Long specialistId,
    String specialistName,
    Long serviceId,
    String serviceName,
    Boolean isActive,
    LocalDateTime createdAt) {
}
