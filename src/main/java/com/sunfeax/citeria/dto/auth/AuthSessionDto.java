package com.sunfeax.citeria.dto.auth;

public record AuthSessionDto(
    LoginResponseDto response,
    String refreshToken
) {}
