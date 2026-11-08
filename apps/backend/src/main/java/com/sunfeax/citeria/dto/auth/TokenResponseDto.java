package com.sunfeax.citeria.dto.auth;

public record TokenResponseDto(
    String accessToken,
    String tokenType
) {}
