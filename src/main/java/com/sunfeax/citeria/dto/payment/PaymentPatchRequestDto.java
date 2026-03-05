package com.sunfeax.citeria.dto.payment;

import com.sunfeax.citeria.enums.PaymentStatus;

import jakarta.annotation.Nullable;

public record PaymentPatchRequestDto(
    @Nullable
    Long appointmentId,

    @Nullable
    PaymentStatus status
) {}
