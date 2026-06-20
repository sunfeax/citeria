package com.sunfeax.citeria.dto.workinghours;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record WorkingHoursResponseDto(
    UUID id,
    UUID specialistId,
    String specialistName,
    UUID businessId,
    String businessName,
    DayOfWeek dayOfWeek,
    LocalTime startTime,
    LocalTime endTime,
    Boolean isActive,
    Instant createdAt
) {}
