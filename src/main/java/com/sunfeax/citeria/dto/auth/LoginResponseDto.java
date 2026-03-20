package com.sunfeax.citeria.dto.auth;

import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;

public record LoginResponseDto(
    String accessToken,
    String tokenType,
    Long id,
    String firstName,
    String lastName,
    UserRole role,
    UserType type
) {}
