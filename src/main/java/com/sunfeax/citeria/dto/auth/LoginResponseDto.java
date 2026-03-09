package com.sunfeax.citeria.dto.auth;

import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;

public record LoginResponseDto(
    String token,
    String tokenType,
    Long id,
    String fullName,
    UserRole role,
    UserType type
) {}
