package com.sunfeax.citeria.dto.appointment;

import java.time.LocalDateTime;

import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.enums.PaymentMethod;

import jakarta.annotation.Nullable;

public record AppointmentPatchRequestDto(
    @Nullable
    Long clientId,

    @Nullable
    Long specialistServiceId,

    @Nullable
    LocalDateTime startTime,

    @Nullable
    LocalDateTime endTime,

    @Nullable
    AppointmentStatus status,

    @Nullable
    PaymentMethod paymentMethod
) {}
