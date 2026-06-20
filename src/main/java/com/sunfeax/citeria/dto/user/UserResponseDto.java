package com.sunfeax.citeria.dto.user;

import java.util.UUID;
import java.time.Instant;

import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;

public record UserResponseDto(
    UUID id,
    String firstName,
    String lastName,
    String email,
    String phone,
    UserRole role,
    UserType type,
    Boolean isActive,
    Instant createdAt
) {}