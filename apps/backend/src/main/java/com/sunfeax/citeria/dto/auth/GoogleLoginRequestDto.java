package com.sunfeax.citeria.dto.auth;

import com.sunfeax.citeria.enums.UserType;

import jakarta.validation.constraints.NotBlank;

public record GoogleLoginRequestDto(
    @NotBlank(message = "Google credential is required")
    String credential,

    UserType type
) {}
