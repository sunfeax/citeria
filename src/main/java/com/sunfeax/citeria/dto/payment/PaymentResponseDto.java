package com.sunfeax.citeria.dto.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.sunfeax.citeria.enums.PaymentStatus;

public record PaymentResponseDto(
    Long id,
    Long appointmentId,
    BigDecimal amount,
    String currency,
    PaymentStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {
}
