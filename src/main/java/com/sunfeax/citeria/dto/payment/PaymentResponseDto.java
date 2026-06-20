package com.sunfeax.citeria.dto.payment;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.Instant;

import com.sunfeax.citeria.enums.PaymentStatus;

public record PaymentResponseDto(
    UUID id,
    UUID appointmentId,
    BigDecimal amount,
    String currency,
    PaymentStatus status,
    Instant createdAt,
    Instant updatedAt) {
}
