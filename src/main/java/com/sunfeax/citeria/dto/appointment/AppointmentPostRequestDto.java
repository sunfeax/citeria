package com.sunfeax.citeria.dto.appointment;

import java.time.LocalDateTime;

import com.sunfeax.citeria.enums.PaymentMethod;

import jakarta.validation.constraints.NotNull;

public record AppointmentPostRequestDto(
    @NotNull(message = "Client id is required")
    Long clientId,

    @NotNull(message = "Specialist service id is required")
    Long specialistServiceId,

    @NotNull(message = "Start time is required")
    LocalDateTime startTime,

    @NotNull(message = "End time is required")
    LocalDateTime endTime,

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod
) {}
