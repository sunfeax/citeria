package com.sunfeax.citeria.dto.appointment;

import jakarta.annotation.Nullable;

/**
 * Mocked card payment payload. The fields are accepted but intentionally NOT validated
 * or charged server-side — the frontend validates the card format and this endpoint always
 * succeeds. Present only to document the request shape and mimic a real payment call.
 */
public record PaymentRequestDto(
    @Nullable String cardNumber,
    @Nullable String cardHolder,
    @Nullable String expiry,
    @Nullable String cvc
) {}
