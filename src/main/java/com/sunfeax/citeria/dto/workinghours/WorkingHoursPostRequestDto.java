package com.sunfeax.citeria.dto.workinghours;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record WorkingHoursPostRequestDto(
    @NotNull(message = "Business id is required")
    UUID businessId,

    @NotNull(message = "Specialist id is required")
    UUID specialistId,

    @NotNull(message = "Day of week is required")
    DayOfWeek dayOfWeek,

    @NotNull(message = "Start time is required")
    LocalTime startTime,

    @NotNull(message = "End time is required")
    LocalTime endTime
) {}
