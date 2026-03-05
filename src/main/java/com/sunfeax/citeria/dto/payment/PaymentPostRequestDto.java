package com.sunfeax.citeria.dto.payment;

import jakarta.validation.constraints.NotNull;

public record PaymentPostRequestDto(
    @NotNull(message = "Appointment id is required")
    Long appointmentId
) {}
