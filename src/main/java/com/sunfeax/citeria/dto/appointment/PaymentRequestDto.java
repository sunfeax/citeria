package com.sunfeax.citeria.dto.appointment;

import jakarta.annotation.Nullable;

public record PaymentRequestDto(
    @Nullable String cardNumber,
    @Nullable String cardHolder,
    @Nullable String expiry,
    @Nullable String cvc
) {}
