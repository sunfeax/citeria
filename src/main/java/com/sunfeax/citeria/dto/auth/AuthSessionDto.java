package com.sunfeax.citeria.dto.auth;

public record AuthSessionDto(
    AuthResponseDto response,
    String refreshToken
) {}
