package com.sunfeax.citeria.dto.appointment;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

import com.sunfeax.citeria.enums.AppointmentStatus;

public record AppointmentResponseDto(
    UUID id,
    UUID clientId,
    String clientName,
    String clientEmail,
    UUID serviceId,
    String serviceName,
    UUID specialistId,
    String specialistName,
    Instant startTime,
    Instant endTime,
    AppointmentStatus status,
    BigDecimal priceAmount,
    Instant paymentDeadline) {
}
