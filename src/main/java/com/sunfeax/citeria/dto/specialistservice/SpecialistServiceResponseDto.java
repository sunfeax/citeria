package com.sunfeax.citeria.dto.specialistservice;

import java.util.UUID;
import java.time.Instant;

public record SpecialistServiceResponseDto(
    UUID id,
    UUID businessId,
    String businessName,
    UUID specialistId,
    String specialistName,
    UUID serviceId,
    String serviceName,
    Boolean isActive,
    Instant createdAt) {
}
