package com.sunfeax.citeria.dto.payment;

import java.util.UUID;
import com.sunfeax.citeria.enums.PaymentStatus;

import jakarta.annotation.Nullable;

public record PaymentPatchRequestDto(
    @Nullable
    UUID appointmentId,

    @Nullable
    PaymentStatus status
) {}
