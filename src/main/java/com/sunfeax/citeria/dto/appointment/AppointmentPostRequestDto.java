package com.sunfeax.citeria.dto.appointment;

import java.util.UUID;
import java.time.Instant;

import jakarta.validation.constraints.NotNull;

public record AppointmentPostRequestDto(
    @NotNull(message = "Service id is required")
    UUID serviceId,

    @NotNull(message = "Start time is required")
    Instant startTime
) {}
