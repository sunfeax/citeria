package com.sunfeax.citeria.dto.appointment;

import java.util.UUID;
import java.time.Instant;

import com.sunfeax.citeria.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;

public record AppointmentPostRequestDto(
    @NotNull(message = "Specialist service id is required")
    UUID specialistServiceId,

    @NotNull(message = "Start time is required")
    Instant startTime,

    @NotNull(message = "End time is required")
    Instant endTime,

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod
) {}
