package com.sunfeax.dobook.dto.user;

import java.time.LocalDateTime;

import com.sunfeax.dobook.enums.UserRole;
import com.sunfeax.dobook.enums.UserType;

public record UserResponseDto(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    UserRole role,
    UserType type,
    LocalDateTime createdAt) {
}
