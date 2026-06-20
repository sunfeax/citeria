package com.sunfeax.citeria.dto.appointment;

import java.util.UUID;
import java.time.Instant;

import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.enums.PaymentMethod;

import jakarta.annotation.Nullable;

public record AppointmentPatchRequestDto(
    @Nullable
    UUID clientId,

    @Nullable
    UUID specialistServiceId,

    @Nullable
    Instant startTime,

    @Nullable
    Instant endTime,

    @Nullable
    AppointmentStatus status,

    @Nullable
    PaymentMethod paymentMethod
) {}
