package com.sunfeax.citeria.dto.auth;

import com.sunfeax.citeria.dto.user.UserResponseDto;

public record AuthResponseDto(
    String accessToken,
    String tokenType,
    UserResponseDto user
) {}
