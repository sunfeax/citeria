package com.sunfeax.citeria.dto.user;

import java.time.LocalDateTime;

import com.sunfeax.citeria.enums.UserRole;
import com.sunfeax.citeria.enums.UserType;

public record UserResponseDto(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phone,
    UserRole role,
    UserType type,
    LocalDateTime createdAt) {
}
