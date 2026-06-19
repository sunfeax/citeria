package com.sunfeax.citeria.dto.appointment;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sunfeax.citeria.enums.AppointmentStatus;
import com.sunfeax.citeria.enums.PaymentMethod;

public record AppointmentResponseDto(
    UUID id,
    UUID clientId,
    String clientName,
    String clientEmail,
    UUID specialistServiceId,
    UUID specialistId,
    String specialistName,
    UUID serviceId,
    String serviceName,
    String businessName,
    LocalDateTime startTime,
    LocalDateTime endTime,
    AppointmentStatus status,
    PaymentMethod paymentMethod,
    BigDecimal priceAmount) {
}
