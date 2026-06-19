package com.sunfeax.citeria.dto.payment;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record PaymentPostRequestDto(
    @NotNull(message = "Appointment id is required")
    UUID appointmentId
) {}
