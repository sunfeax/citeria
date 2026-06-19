package com.sunfeax.citeria.dto.payment;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sunfeax.citeria.enums.PaymentStatus;

public record PaymentResponseDto(
    UUID id,
    UUID appointmentId,
    BigDecimal amount,
    String currency,
    PaymentStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
}
