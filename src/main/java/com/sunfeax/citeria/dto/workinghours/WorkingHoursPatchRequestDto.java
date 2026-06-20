package com.sunfeax.citeria.dto.workinghours;

import java.time.LocalTime;

import jakarta.annotation.Nullable;

public record WorkingHoursPatchRequestDto(
    @Nullable
    LocalTime startTime,

    @Nullable
    LocalTime endTime,

    @Nullable
    Boolean isActive
) {}
